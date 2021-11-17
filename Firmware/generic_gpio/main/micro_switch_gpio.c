//Creator:Jesse Ray

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "freertos/queue.h"
#include "driver/gpio.h"

//#include"gpio_example_main.h"

//Defines for selection of the input pins
#define GPIO_INPUT_IO_19     19
#define GPIO_INPUT_PIN_SEL  (1ULL<<GPIO_INPUT_IO_19)

//Init function for the micro switch (Really just initializing the gpio that reads the micro switch)
void init_micro_switch(void)
{
    gpio_config_t io_conf;
    //No interrupt
    io_conf.intr_type = GPIO_INTR_DISABLE;
    //bit mask of the pins, use GPIO19 here
    io_conf.pin_bit_mask = GPIO_INPUT_PIN_SEL;
    //set as input mode
    io_conf.mode = GPIO_MODE_INPUT;
    //enable pull-up mode
    io_conf.pull_up_en = 1;
    gpio_config(&io_conf);

}

//Function that runs the code for checking GPIO19
void app_main(void)
{
    init_micro_switch();
    uint32_t pin_value;
    int cnt = 0;
    while(1) {
        printf("cnt: %d\n", cnt++);
        vTaskDelay(1000 / portTICK_RATE_MS);
        pin_value = gpio_get_level(GPIO_INPUT_IO_19);
        printf("%d is the value at " ,pin_value);
    }
}
