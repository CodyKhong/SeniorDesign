/* UART asynchronous example, that uses separate RX and TX tasks

   This example code is in the Public Domain (or CC0 licensed, at your option.)

   Unless required by applicable law or agreed to in writing, this
   software is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
   CONDITIONS OF ANY KIND, either express or implied.
*/
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_system.h"
#include "esp_log.h"
#include "driver/uart.h"
#include "string.h"
#include "driver/gpio.h"

static const int RX_BUF_SIZE = 1024;

#define TXD_PIN (GPIO_NUM_25)
#define RXD_PIN (GPIO_NUM_26)

void init(void) {
    const uart_config_t uart_config = {
        .baud_rate = 115200,
        .data_bits = UART_DATA_8_BITS,
        .parity = UART_PARITY_DISABLE,
        .stop_bits = UART_STOP_BITS_1,
        .flow_ctrl = UART_HW_FLOWCTRL_DISABLE,
        .source_clk = UART_SCLK_APB,
    };
    // We won't use a buffer for sending data.
    uart_driver_install(UART_NUM_1, RX_BUF_SIZE * 2, 0, 0, NULL, 0);
    uart_param_config(UART_NUM_1, &uart_config);
    uart_set_pin(UART_NUM_1, TXD_PIN, RXD_PIN, UART_PIN_NO_CHANGE, UART_PIN_NO_CHANGE);
}

int sendData(const char* logName, const char* data)
{
    int len = sizeof(data);
    printf("%d # of bytes; %s was written\n", len, data);
    const int txBytes = uart_write_bytes(UART_NUM_1, data, len);
    ESP_LOGI(logName, "Wrote %d bytes", txBytes);
    return txBytes;
}

/*  sendData variance for sending hex data
    only really need it of 0x1a
 */
/* int sendHex(const char* logName,const char* hex)
{


} */


/*  A9G GPS connection
    GPS task needs to be only triggered when it's needed
    *TODO:  
    
*/
static void gpsSetup()
{
    static const char *GPS_TAG = "GPS_TASK";
    esp_log_level_set(GPS_TAG, ESP_LOG_INFO);
    sendData(GPS_TAG, "AT+GPS=1\r\n");
}

static void gpsDisable()
{
    static const char *GPS_TAG = "GPS_TASK";
    esp_log_level_set(GPS_TAG, ESP_LOG_INFO);
    sendData(GPS_TAG, "AT+GPS=0\r\n");

}

/* A9G internet connection
    APN:    hologram
    User:   n/a
    Pass:   n/a
    *TODO:  need to make sure we return "OK"


*/
static void internetSetup()
{
    static const char *GPRSGSM_TAG = "GPRSGSM_TASK";
    esp_log_level_set(GPRSGSM_TAG, ESP_LOG_INFO);
    sendData(GPRSGSM_TAG, "AT+CGATT=1\r\n");
    vTaskDelay(2000 / portTICK_PERIOD_MS);
    
    sendData(GPRSGSM_TAG, "AT+CGDCONT=1,\"IP\",\"hologram\"\r\n");
    vTaskDelay(3000 / portTICK_PERIOD_MS);
    
    sendData(GPRSGSM_TAG, "AT+CGACT=1,1\r\n");
    vTaskDelay(2000 / portTICK_PERIOD_MS);
    

    return;
}

/*
    SMS setup turns on format for receiving
    and sending format 'CMGS="1phonenumber" >(text data) then send hex (0x1a)

*/
static void smsSetup()
{
    static const char *SMS_TAG = "sms_TASK";
    esp_log_level_set(SMS_TAG, ESP_LOG_INFO);
    sendData(SMS_TAG, "AT+CMGF=1\r\n");
    
}

/*
    Disable SMS feauture when not needed
*/
static void smsDisable()
{
    static const char *SMSDISABLE_TAG = "Disable_SMS";
    esp_log_level_set(SMSDISABLE_TAG, ESP_LOG_INFO);
    sendData(SMSDISABLE_TAG, "AT+CMGF=0\r\n");
    
}

/*
    Get location from GPS information
    parsing location results from 'AT+LOCATION=2'
    String[] latlong =  "-34.8799074,174.7565664".split(",");
    double latitude = Double.parseDouble(latlong[0]);
    double longitude = Double.parseDouble(latlong[1]);
*/
/* static float getLocation()
{
    // char latlong[] = "";
    // char res = "\0";
    // res = latlong.replace(",","");
    // float result = float.parseFloat(res);
    static const char *getLoc_TAG = "getLocation";

    smsSetup();
    vTaskDelay(1000 / portTICK_PERIOD_MS);
    esp_log_level_set(getLoc_TAG, ESP_LOG_INFO);
    sendData(getLoc_TAG, "AT+GPRSD=1\r\n");
    vTaskDelay(2000 / portTICK_PERIOD_MS);
    sendData(getLoc_TAG, "AT+GPRSD=0\r\n");
    vTaskDelay(1000 / portTICK_PERIOD_MS);
    sendData(getLoc_TAG, "AT+LOCATION=2\r\n");
    vTaskDelay(1000 / portTICK_PERIOD_MS);
    gpsDisable();

    return result;
} */

/*
    Send location of user using a predefined google hyperlink
    https://www.google.com/maps/search/?api=1&query=latitude,longitude

*/
static void sendLocation()
{
    smsSetup();
    vTaskDelay(2000 / portTICK_PERIOD_MS);
    //getLocation();
    char* googs = "https://www.google.com/maps/search/?api=1&query=  ";
    char* result =  "13241.1,-1252.999 \r\n";
    char * hyperlink = (char *) malloc(1 + strlen(googs)+ strlen(result) );
    strcpy(hyperlink, googs);
    strcpy(hyperlink, result);
    char hex_byte_for_register[]= {0x1a};

    static const char *sendLoc_TAG = "Location_SMS";
    esp_log_level_set(sendLoc_TAG, ESP_LOG_INFO);
    sendData(sendLoc_TAG, "AT+CMGS=\"14077564031\"\r\n");
    vTaskDelay(2000 / portTICK_PERIOD_MS);
    
    sendData(sendLoc_TAG, hyperlink);
    //sendData(sendLoc_TAG, "TEST\r\n");
    sendData(sendLoc_TAG,  hex_byte_for_register); //ctrl+z equivalent
    sendData(sendLoc_TAG, "\r\n"); //ctrl+z equivalent
    vTaskDelay(5000 / portTICK_PERIOD_MS);
    
    smsDisable();
    free(hyperlink);
}

/*  RX task for debugging
    shows A9G response to commands
*/
void rx_task()
{
    static const char *RX_TASK_TAG = "RX_TASK";
    esp_log_level_set(RX_TASK_TAG, ESP_LOG_INFO);
    uint8_t* data = (uint8_t*) malloc(RX_BUF_SIZE+1);
    while(1)
    {
        const int rxBytes = uart_read_bytes(UART_NUM_1, data, RX_BUF_SIZE, 1000 / portTICK_RATE_MS);
        if (rxBytes > 0) {
            data[rxBytes] = 0;
            ESP_LOGI(RX_TASK_TAG, "Read %d bytes: '%s'", rxBytes, data);
            ESP_LOG_BUFFER_HEXDUMP(RX_TASK_TAG, data, rxBytes, ESP_LOG_INFO);
            }
    }
     free(data);
}

void app_main(void)
{

    //INITIAL SETUP PROCEDURES
    init();
    xTaskCreate(rx_task, "uart_rx_task", 1024*2, NULL, configMAX_PRIORITIES, NULL);
    //internetSetup();
    sendLocation();

}
