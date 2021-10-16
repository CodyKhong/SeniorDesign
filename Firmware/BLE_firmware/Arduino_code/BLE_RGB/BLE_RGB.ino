//#include <dummy.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <NeoPixelBus.h>

#define SERVICE_UUID           "0000ffe0-0000-1000-8000-00805f9b34fb" // UART service UUID
#define CHARACTERISTIC_UUID_RX "0000ffe1-0000-1000-8000-00805f9b34fb"
//#define CHARACTERISTIC_UUID_TX "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
#define colorSaturation 255
#define MAX_DATA_SIZE 128   // Max transmit data size in bit

#define LED_STATE 0x1
#define LED_COLOR 0x2
#define LED_ON 0x10
#define LED_OFF 0x11

const uint16_t PixelCount = 60; // this example assumes 4 pixels, making it smaller will cause a failure
const uint8_t PixelPin = 18;  // make sure to set this to the correct pin, ignored for Esp8266

// three element pixels, in different order and speeds
NeoPixelBus<NeoGrbFeature, Neo800KbpsMethod> strip(PixelCount, PixelPin);

RgbColor white(colorSaturation, colorSaturation, colorSaturation);
RgbColor red(colorSaturation, 0, 0);
RgbColor black(0);

hw_timer_t * timer = NULL;
const int LED = GPIO_NUM_2; // on board blue led. (pin24)
const char ledR = A4;
const char ledG = A5;
const char ledB = A18;
//const int LED = 24;

uint8_t ledArray[3] = {1, 2, 3};

bool deviceConnected = false; // connection-status flag

static int commandLength = 0;
char receivedCommand[MAX_DATA_SIZE] = {}; // commands via bluetooth stored here

void IRAM_ATTR onTimer() {
  static byte state = LOW;
  state = ! state;
  digitalWrite(LED, state);
}

class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      Serial.println("connnected to a device!");
    }
    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
      Serial.println("disconnected!");
    }
};

class MyCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pCharacteristic) {
      std::string rxValue = pCharacteristic->getValue();
      commandLength = rxValue.length();
      if (commandLength > 0) {
        Serial.println("Received a command\n");
        strcpy(receivedCommand, rxValue.c_str());
//        for(int i = 0; i < commandLength; i++) 
//        {
//          printf("%x", receivedCommand[i]);
//        }
//        printf("\n");
//        receivedCommand = rxValue.c_str();
//        printf("%x", receivedCommand);
        rxValue = "";
      }
    }
};

void setup() {
 
  // this resets all the neopixels to an off state
  strip.Begin();
  strip.Show();
  // Create the BLE Device
  BLEDevice::init("this_is_the_fucking_BLE"); // Give it a name

  // Create the BLE Server
  BLEServer *pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pService = pServer->createService(SERVICE_UUID);

  // Create a BLE
  /*BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                      CHARACTERISTIC_UUID_TX,
                      BLECharacteristic::PROPERTY_NOTIFY
                    );
    pCharacteristic->addDescriptor(new BLE2902());*/
  BLECharacteristic *pCharacteristic = pService->createCharacteristic(
                                         CHARACTERISTIC_UUID_RX,
                                         BLECharacteristic::PROPERTY_WRITE
                                       );
  pCharacteristic->setCallbacks(new MyCallbacks());

  // Start the service
  pService->start();

  // Start advertising
  pServer->getAdvertising()->start();

  timer = timerBegin(0, 80, true);
  timerAttachInterrupt(timer, &onTimer, true);
  timerAlarmWrite(timer, 1000000, true);

  Serial.begin(115200);
  Serial.println("Waiting a client connection to notify...");
  pinMode(LED, OUTPUT);

  ledcAttachPin(ledR, 1); // assign RGB led pins to channels
  ledcAttachPin(ledG, 2);
  ledcAttachPin(ledB, 3);

  ledcSetup(1, 12000, 8); // 12 kHz PWM, 8-bit resolution
  ledcSetup(2, 12000, 8);
  ledcSetup(3, 12000, 8);

}

void apply_color(unsigned char R, unsigned char G, unsigned char B)
{
  if (R >= 0 && R <= 255)  ledcWrite(1, R);
  else ledcWrite(1, 255);

  if (G >= 0 && G <= 255) ledcWrite(2, G);
  else ledcWrite(2, 255);

  if (B >= 0 && B <= 255) ledcWrite(3, B);
  else ledcWrite(3, 255);

  Serial.println("LED's color changed!");

 Serial.print(R);
 Serial.print(G);
 Serial.print(B);
}

void change_color(String cmd) {

  if (cmd.substring(0, 1) == "R")
  {
    //version 5 convert decimal to hex
//     long long number = strtoll( &cmd[1], NULL, 16);
//     // Split them up into r, g, b values
//     long long R = number >> 16;
//     long long G = number >> 8 & 0xFF;
//     long long B = number & 0xFF;
//     apply_color(R, G, B);
    
    //version 6 convert decimal to char
     char  *char_cmd = &cmd[0]; // assign char dau tien cua string vao con tro char
  
  if (char_cmd[0] == '#')
  {
     int R = (int) char_cmd[1] *255/100;
     int G = (int) char_cmd[2] *255/100;
     int B = (int) char_cmd[3]  *255/100;

    apply_color(R, G, B);
  }
  else
    Serial.println("Invalid command!");

  }
}

void blink_1()  {
  Serial.println("Received a blinking require command!");
  digitalWrite(LED, HIGH);
  delay(500);
  digitalWrite(LED, LOW);
  delay(500);
  digitalWrite(LED, HIGH);
  delay(500);
  digitalWrite(LED, LOW);
  delay(500);
}

int ledState(byte state)
{
  if(state == LED_ON)
    {
      printf("LED ON\n");
      strip.SetPixelColor(0, white);
      strip.SetPixelColor(1, white);
      strip.SetPixelColor(2, white);
      strip.SetPixelColor(3, white);
      strip.SetPixelColor(4, white);
      strip.Show();
    }
    
    else
    {
      // turn off the pixels
      printf("LED OFF\n");
      strip.SetPixelColor(0, black);
      strip.SetPixelColor(1, black);
      strip.SetPixelColor(2, black);
      strip.SetPixelColor(3, black);
      strip.SetPixelColor(4, black);
      strip.Show();
    }
  return 0;
}

int ledColor(byte red, byte green, byte blue)
{ 
  printf("red: %02x, green: %02x, blue: %02x\n", red, green, blue);
  return 0;
}

void readCommand()
{
  byte cmdType = receivedCommand[0];
  byte cmdSize = receivedCommand[1];
  
  switch(cmdType)
  {
    case LED_STATE:
    {
      byte state = receivedCommand[2];
      ledState(state);
      break;
    } 
    case LED_COLOR:
    {
      byte red = receivedCommand[2];
      byte green = receivedCommand[3];
      byte blue = receivedCommand[4];
      ledColor(red, green, blue);
      break;
    }
    
    default:
      break;      
  }
  
}

void loop() {
  delay(10);
  if (commandLength > 0)
  {
    readCommand();
//    static int cnt = 0;
//    cnt++;
////    printf("%d\n", cnt);
////    printf("%d\n", cnt%2);
//    delay(100);
//    printf("This is what I got: ", receivedCommand);
//    for(int i = 0; i < commandLength; i++) 
//    {
//      printf("%x", receivedCommand[i]);
//    }
//    printf("\n");
//    if(cnt%2 == 0)
//    {
//    strip.SetPixelColor(0, red);
//    strip.SetPixelColor(1, red);
//    strip.SetPixelColor(2, red);
//    strip.SetPixelColor(3, red);
//    strip.SetPixelColor(4, red);
//    strip.Show();
//    }
//    
//    else
//    {
//    // turn off the pixels
//    strip.SetPixelColor(0, black);
//    strip.SetPixelColor(1, black);
//    strip.SetPixelColor(2, black);
//    strip.SetPixelColor(3, black);
//    strip.SetPixelColor(4, black);
//    strip.Show();
//    }
    
    if (receivedCommand == "BL1")
    {
      blink_1();
    }

    else if (receivedCommand == "ONLED" || receivedCommand == "1")
    {
      digitalWrite(LED, HIGH);
    }

    else if (receivedCommand == "OFFLED" || receivedCommand == "0")
    {
      digitalWrite(LED, LOW);
    }

    else if (receivedCommand == "BLN")
    {
      timerAlarmEnable(timer);
    }

    else if (receivedCommand == "SBL")
    {
      timerAlarmDisable(timer);
    }

    else
    {
      change_color(receivedCommand);
    }

    commandLength = 0;
  }
  //delay(10);
}
