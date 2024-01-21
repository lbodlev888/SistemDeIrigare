#include <ESP8266WiFi.h>
#include <ESP8266HTTPClient.h>
#include <DHT_Async.h>

#define DHT_SENSOR_TYPE DHT_TYPE_11
#define ssid "BodlevCorporation"
#define pass "12345678"
#define pompa D1

WiFiServer server(80);
static const int DHT_SENSOR_PIN = D0;
DHT_Async dht_sensor(DHT_SENSOR_PIN, DHT_SENSOR_TYPE);

void setup()
{
  Serial.begin(115200);
  pinMode(pompa, OUTPUT);
  pinMode(A0, INPUT);
  pinMode(DHT_SENSOR_PIN, INPUT);
  digitalWrite(pompa, HIGH);
  WiFi.mode(WIFI_STA);
  WiFi.begin(ssid, pass);
  while(WiFi.status() != WL_CONNECTED)
  {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("IP address: ");
  Serial.println(WiFi.localIP());
  server.begin();
}

String header;
int lastUpdate = millis(), lastMillis = 0;
float temp, humid, humidApa;
bool autonom = false;

void loop()
{
  if(millis() - lastUpdate >= 400) {
    dht_sensor.measure(&temp, &humid);
    lastUpdate = millis();
    humidApa = analogRead(A0);
  } //prevenire crash la placa
  WiFiClient client = server.available();

  if (client) {
    String currentLine = "";
    while (client.connected()) {
      if (client.available()) {
        char c = client.read();
        header += c;
        if (c == '\n') {
          if (currentLine.length() == 0) {
            client.println("HTTP/1.1 200 OK");
            client.println("Content-type:text/html");
            client.println("Connection: close");
            client.println();
            if(header.indexOf("GET /?pomp=yes") >= 0) {
              Serial.println("Pompa este pornita");
              digitalWrite(pompa, LOW);
            }
            if(header.indexOf("GET /?pomp=no") >= 0) {
              Serial.println("Pompa este oprita");
              digitalWrite(pompa, HIGH);
            }
            if(header.indexOf("GET /?auto=yes") >= 0) {
              autonom = true;
              lastMillis = millis() + 1800000;
              Serial.println("Modul autonom este pornit");
            }
            if(header.indexOf("GET /?auto=no") >= 0) {
              autonom = false;
              Serial.println("Modul autonom este oprit");
            }
            client.print("Temperatura: ");
            client.print(temp);
            client.println(" C");
            client.print("$Umiditate in aer: ");
            client.print(humid);
            client.println("%\n");
            client.print("$Umiditate in sol: ");
            client.print(humidApa);
            client.println();
            break;
          } else {
            currentLine = "";
          }
        } else if (c != '\r') {
          currentLine += c;
        }
      }
    }
    header = "";
    client.stop();
  }
  if(autonom && millis() >= lastMillis) {
    if(analogRead(A0) < 200) {
      digitalWrite(pompa, HIGH);
      int start = millis();
      if(millis() >= start + 15000) {
        digitalWrite(pompa, LOW);
        lastMillis = millis() + 1800000;
      }
    }
  }
}