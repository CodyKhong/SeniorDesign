//#include <dummy.h>

#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <NeoPixelBus.h>

#define SERVICE_UUID           "0000ffe0-0000-1000-8000-00805f9b34fb" // UART service UUID
#define CHARACTERISTIC_UUID_RX "0000ffe1-0000-1000-8000-00805f9b34fb"
#define CHARACTERISTIC_UUID_TX "0000ffe2-0000-1000-8000-00805f9b34fb"
#define colorSaturation 255
#define MAX_DATA_SIZE 128   // Max transmit data size in bit

#define LED_STATE 0x01
#define LED_COLOR 0x02
#define TEMP_REQ  0x03
#define TEMP_DATA 0x04
#define ACC_REQ   0x05
#define ACC_DATA  0x06
#define SPK_CMD   0x07

#define LED_OFF   0x10
#define LED_ON    0x11

#define SPK_OFF   0x70
#define SPK_ON    0x71

const uint16_t PixelCount = 60; // this example assumes 4 pixels, making it smaller will cause a failure
const uint8_t PixelPin = 18;  // make sure to set this to the correct pin, ignored for Esp8266

// three element pixels, in different order and speeds
NeoPixelBus<NeoGrbFeature, Neo800KbpsMethod> strip(PixelCount, PixelPin);

static byte red = 0xFF;
static byte green = 0xFF;
static byte blue = 0xFF;
static byte lastR = 0xFF;
static byte lastG = 0xFF;
static byte lastB = 0xFF;
// bool colorInit = 0;

RgbColor COLOR_WHITE(colorSaturation, colorSaturation, colorSaturation);
RgbColor COLOR_RED(colorSaturation, 0, 0);
RgbColor COLOR_BLACK(0);
// RgbColor Phone((uint8_t)red, (uint8_t)green, (uint8_t)blue);

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

void IRAM_ATTR onTimer() 
{
  static byte state = LOW;
  state = !state;
  digitalWrite(LED, state);
}

class MyServerCallbacks: public BLEServerCallbacks 
{
    void onConnect(BLEServer* pServer) 
    {
      deviceConnected = true;
      Serial.println("connnected to a device!");
    }

    void onDisconnect(BLEServer* pServer) 
    {
      deviceConnected = false;
      Serial.println("disconnected!");
      
      // Start advertising
      pServer->getAdvertising()->start();
      Serial.println("Waiting a client connection to notify...");
    }
};

class MyCallbacks: public BLECharacteristicCallbacks 
{
  void onWrite(BLECharacteristic *pCharacteristic) 
  {
    std::string rxValue = pCharacteristic->getValue();
    commandLength = rxValue.length();
    if (commandLength > 0) 
    {
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
  BLEDevice::init("Collar_Companion"); // Give it a name

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

// void setup()
// {
//   bleInit();
// }

/*
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

void change_color(String cmd) 
{
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

void blink_1()  
{
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
*/

int ledColor()
{ 
  printf("red: %02x, green: %02x, blue: %02x\n", red, green, blue);
  RgbColor color((uint8_t)red, (uint8_t)green, (uint8_t)blue);
  strip.SetPixelColor(0, color);
  strip.SetPixelColor(1, color);
  strip.SetPixelColor(2, color);
  strip.SetPixelColor(3, color);
  strip.SetPixelColor(4, color);
  strip.Show();

  return 0;
}

int ledState(byte state)
{
  if (state == LED_ON)
  {
    red = lastR;
    green = lastG;
    blue = lastB;
  }

  else
  {
    lastR = red;
    delay(1);
    red = 0;

    lastG = green;
    delay(1);
    green = 0;

    lastB = blue;
    delay(1);
    blue = 0;
  }

  ledColor();

  // if(state == LED_ON)
  // {
  //   colorInit = 1;
  //   printf("LED ON\n");
  //   strip.SetPixelColor(0, COLOR_WHITE);
  //   strip.SetPixelColor(1, COLOR_WHITE);
  //   strip.SetPixelColor(2, COLOR_WHITE);
  //   strip.SetPixelColor(3, COLOR_WHITE);
  //   strip.SetPixelColor(4, COLOR_WHITE);
  //   strip.Show();     
  // }
    
  // else
  // {
  //   // turn off the pixels
  //   printf("LED OFF\n");
  //   strip.SetPixelColor(0, COLOR_BLACK);
  //   strip.SetPixelColor(1, COLOR_BLACK);
  //   strip.SetPixelColor(2, COLOR_BLACK);
  //   strip.SetPixelColor(3, COLOR_BLACK);
  //   strip.SetPixelColor(4, COLOR_BLACK);
  //   strip.Show();
  // }

  return 0;
}

// String createCMD(byte dataType, byte dataSize, byte *cmdData)
// {
//   uint8_t cmd[dataSize + 2];
//   cmd[0] = dataType;
//   cmd[1] = dataSize;
//   for(int i = 0; i < dataSize; i++)
//   {
//     cmd[i + 2] = cmdData[i];
//   }
//   cmd[dataSize + 1] = '\0';

//   return cmd;
// }

// // Receive data, send data through BLE, return 0 if data transmit success
// int sendData(String cmd)
// {
//   pCharacteristic->setValue(cmd);
  
//   return 0;
// }

// int sendTemp()
// {
//   byte *data = {0x55};
//   printf("Received temp request.\n");
//   sendData(createCMD(TEMP_DATA, 1, data));
//   printf("Temp data sent.\n");

//   return 0;
// }

// int sendAcc()
// {
//   printf("Received accelerometer request.\n");
 
//   return 0;
// }

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
      red = receivedCommand[2];
      green = receivedCommand[3];
      blue = receivedCommand[4];
      ledColor();
      break;
    }

    // case TEMP_REQ:
    // {
    //   sendTemp();
    //   break;
    // }

    // case ACC_REQ:
    // {
    //   sendAcc();
    //   break;
    // }

    default:
      break;      
  }
  
}

void loop() {
  delay(10);
  if (commandLength > 0)
  {
    readCommand();

  /*
   static int cnt = 0;
   cnt++;
//    printf("%d\n", cnt);
//    printf("%d\n", cnt%2);
   delay(100);
   printf("This is what I got: ", receivedCommand);
   for(int i = 0; i < commandLength; i++) 
   {
     printf("%x", receivedCommand[i]);
   }
   printf("\n");
   if(cnt%2 == 0)
   {
   strip.SetPixelColor(0, red);
   strip.SetPixelColor(1, red);
   strip.SetPixelColor(2, red);
   strip.SetPixelColor(3, red);
   strip.SetPixelColor(4, red);
   strip.Show();
   }
   
   else
   {
   // turn off the pixels
   strip.SetPixelColor(0, black);
   strip.SetPixelColor(1, black);
   strip.SetPixelColor(2, black);
   strip.SetPixelColor(3, black);
   strip.SetPixelColor(4, black);
   strip.Show();
   }
    
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
*/

    commandLength = 0;
  }
}
