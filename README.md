# Start-Kommando
java -jar ./target/racepoint-api-0.0.1-SNAPSHOT.jar -g gitClient_test.json -o otherClient_test.json -s server_test.json -k admin_keys.json

# Curl für Admin-Login
curl -H "Content-Type: application/json" -X POST -d '{"userName":"test","password":"TestPasswort"}' localhost:8080/admin/login



# Installation
-> Docker installieren
sudo apt-get install docker-io
-> sudo-Rechte an Docker anpassen; $USER mit eigenem Usernamen ersetzen
sudo groupadd docker
sudo usermod -aG docker $USER
-> System neustarten, um Userrechte systemweit zu aktualisieren