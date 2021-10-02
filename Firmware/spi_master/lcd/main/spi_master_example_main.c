/* SPI Master example

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "driver/spi_master.h"
#include "driver/gpio.h"

#include "pretty_effect.h"

/*
 This code displays some fancy graphics on the 320x240 LCD on an ESP-WROVER_KIT board.
 This example demonstrates the use of both spi_device_transmit as well as
 spi_device_queue_trans/spi_device_get_trans_result and pre-transmit callbacks.

 Some info about the ILI9341/ST7789V: It has an C/D line, which is connected to a GPIO here. It expects this
 line to be low for a command and high for data. We use a pre-transmit callback here to control that
 line: every transaction has as the user-definable argument the needed state of the D/C line and just
 before the transaction is sent, the callback will set this line to the correct state.
*/

#ifdef CONFIG_IDF_TARGET_ESP32
#define LCD_HOST    HSPI_HOST
#define DMA_CHAN    2

#define PIN_NUM_MISO 13
#define PIN_NUM_MOSI 12
#define PIN_NUM_CLK  14
#define PIN_NUM_CS   15

#define PIN_NUM_DC   21
#define PIN_NUM_RST  18
#define PIN_NUM_BCKL 5
#elif defined CONFIG_IDF_TARGET_ESP32S2
#define LCD_HOST    SPI2_HOST
#define DMA_CHAN    LCD_HOST

#define PIN_NUM_MISO 37
#define PIN_NUM_MOSI 35
#define PIN_NUM_CLK  36
#define PIN_NUM_CS   34

#define PIN_NUM_DC   4
#define PIN_NUM_RST  5
#define PIN_NUM_BCKL 6
#elif defined CONFIG_IDF_TARGET_ESP32C3
#define LCD_HOST    SPI2_HOST
#define DMA_CHAN    LCD_HOST

#define PIN_NUM_MISO 2
#define PIN_NUM_MOSI 7
#define PIN_NUM_CLK  6
#define PIN_NUM_CS   10

#define PIN_NUM_DC   9
#define PIN_NUM_RST  18
#define PIN_NUM_BCKL 19
#endif

#define GPIO_OUTPUT_PIN_SEL (1ULL << PIN_NUM_CS)
void cs_gpio_settings()
{
    gpio_config_t io_conf;
    //disable interrupt
    io_conf.intr_type = GPIO_PIN_INTR_DISABLE;
    //set as output mode
    io_conf.mode = GPIO_MODE_OUTPUT;
    //bit mask of the of the pins you want to set eg cs PIN
    io_conf.pin_bit_mask = GPIO_OUTPUT_PIN_SEL;
    //disable pulldowm mode
    io_conf.pull_down_en = 0;
    //disable pullup mode
    io_conf.pull_up_en = 0;
    //configure GPIO with the given settings
    gpio_config(&io_conf);
}
void cs_low()
{
    gpio_set_level(PIN_NUM_CS,0);
}

void cs_high()
{
    gpio_set_level(PIN_NUM_CS,1);
}


bool lis2dw12_read_reg(spi_device_handle_t spi, uint8_t reg, uint8_t *data)
{
    //esp_err_t ret
    spi_transaction_t t;

    cs_low(); // Function for pulling cs low to start SPI communication

    reg |= 0x80;
    memset(&t, 0, sizeof(t)); // Zero out the transaction
    t.length = 8; // command is 8 bits
    t.tx_buffer = &reg; // The data is command itself
    spi_device_transmit(spi, &t);

    memset(&t, 0, sizeof(t));
    t.length = 8 * 2;
    t.flags = SPI_TRANS_USE_RXDATA;
    spi_device_transmit(spi, &t);

    cs_high(); //function to pull cs high and end SPI communication

    *data = t.rx_data[0];
    return true;
}

bool lis2dw12_write_reg(spi_device_handle_t spi, uint8_t reg, uint8_t data)
{
    //esp_err_t ret
    spi_transaction_t t;
    uint8_t buf[2];
    esp_err_t ret;

    cs_low(); // Function for pulling cs low to start SPI communication

    reg &= 0x7f;
    buf[0] = reg;
    buf[1] = data;
    printf("Value: %02X, %02X \n", buf[0], buf[1]);
    memset(&t, 0, sizeof(t)); // Zero out the transaction
    t.length = 8 * 2; // command is 8 bits
    t.tx_buffer = buf; // The data is command itself
    //t.flags = SPI_TRANS_USE_TXDATA;

    ret = spi_device_transmit(spi, &t);
    //t.length = 8 * 2; // command is 8 bits
    if (ret != ESP_OK)
    {
        return false;
    }
    
    cs_high(); //function to pull cs high and end SPI communication

    return true;
}

//read chip id lis2dw12
void lis2dw12_spi_read_chip_id(spi_device_handle_t spi)
{
    uint8_t reg = 0x0F; // Chip id register (Who am I register)
    uint8_t id;

    lis2dw12_read_reg(spi, reg, &id);
    printf("Chip id: %02X\n", id);
}

void wr_reg_test(spi_device_handle_t spi)
{
    bool ret;
    uint8_t write_reg = 0x21;
    uint8_t write_data = 0x00;

    ret = lis2dw12_write_reg(spi, write_reg, write_data);
    uint8_t read_reg = 0x21;
    uint8_t read_data;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    printf("Read data: %02x\n", read_data);

    //uint8_t write_reg = 0x21;
    write_data = 0x04;

    ret = lis2dw12_write_reg(spi, write_reg, write_data);
    if (!ret)
    {
        printf("Hi fuckers\n");
    }

    // uint8_t read_reg = 0x21;
    // uint8_t read_data;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    printf("Read data: %02x\n", read_data);
}

void read_xaxis(spi_device_handle_t spi)
{
    uint8_t read_reg = 0x29;
    uint8_t read_data;
    int16_t data_storage = 0;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage = read_data;
    data_storage = data_storage << 8;
    
    read_reg = 0x28;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage |= read_data;
    //printf("%i\n", data_storage); //raw value
    data_storage = data_storage * 0.061;
    printf("Read x data: %i\n", data_storage);
}

void read_yaxis(spi_device_handle_t spi)
{
    uint8_t read_reg = 0x2B;
    uint8_t read_data;
    int16_t data_storage = 0;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage = read_data;
    data_storage = data_storage << 8;

    read_reg = 0x2A;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage |= read_data;
    data_storage = data_storage * 0.061;
    printf("Read y data: %i\n", data_storage);
}

void read_zaxis(spi_device_handle_t spi)
{
    uint8_t read_reg = 0x2D;
    uint8_t read_data;
    int16_t data_storage = 0;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage = read_data;
    data_storage = data_storage << 8;

    read_reg = 0x2C;

    lis2dw12_read_reg(spi, read_reg, &read_data);
    data_storage |= read_data;
    data_storage = data_storage * 0.061;
    printf("Read z data: %i\n\n\n", data_storage);
}


void app_main(void)
{

    esp_err_t ret;
    spi_device_handle_t spi;
    spi_bus_config_t buscfg={
        .miso_io_num=PIN_NUM_MISO,
        .mosi_io_num=PIN_NUM_MOSI,
        .sclk_io_num=PIN_NUM_CLK,
        .quadwp_io_num=-1,
        .quadhd_io_num=-1,
    };
    spi_device_interface_config_t devcfg={
#ifdef CONFIG_LCD_OVERCLOCK
        .clock_speed_hz=26*1000*1000,           //Clock out at 26 MHz
#else
        .clock_speed_hz=10*1000*1000,           //Clock out at 10 MHz
#endif
        .mode=0,                                //SPI mode 0
        .spics_io_num=-1,               //CS pin
        .queue_size=7,                          //We want to be able to queue 7 transactions at a time
    };
    //Initialize the SPI bus
    ret=spi_bus_initialize(LCD_HOST, &buscfg, DMA_CHAN);
    ESP_ERROR_CHECK(ret);
    //Attach the LCD to the SPI bus
    ret=spi_bus_add_device(LCD_HOST, &devcfg, &spi);
    ESP_ERROR_CHECK(ret);
    //Setup delay for later use
    const TickType_t xDelay = 625 / portTICK_PERIOD_MS;

    cs_gpio_settings();

    lis2dw12_spi_read_chip_id(spi);

    wr_reg_test(spi);

    lis2dw12_write_reg(spi, 0x20, 0x10); // Control register 1, low power mode 1.6Hz
    lis2dw12_write_reg(spi, 0x25, 0x04); // Control register 6, low noise on

    while(1)
    {
        read_xaxis(spi);
        // vTaskDelay(xDelay);

        read_yaxis(spi);
        // vTaskDelay(xDelay);

        read_zaxis(spi);
        vTaskDelay(xDelay);

    }
}