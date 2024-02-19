import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

    /* Go Types:
     * 1 : up
     * 2 : down
     * 3 : left top
     * 4 : left bottom
    */


public class Generator{

    private Pixel[][] code = new Pixel[21][21]; // End Array 21x21

    private Pixel[][] bytes = new Pixel[19][8]; // 19 Bytes

    private Pixel[] enctype = {new Pixel(false, 6), new Pixel(true, 6), new Pixel(false, 6), new Pixel(false, 6)}; // Encryption type: byte encoding

    private Pixel[] msgend = {new Pixel(false, 7), new Pixel(false, 7), new Pixel(false, 7), new Pixel(false, 7)}; // Message End bits

    private String message = ""; // Zu encodierende Nachricht

    private int masktype = -1; // Mask Pattern Typ

    private String formatInfoRaw; // Final Format Information

    private JsonReader jsonreader = new JsonReader(); // Neuer Json Reader

    public Generator(String text, int msktyp){

        masktype = msktyp;  // Attribute setzen
        message = text;

        for(int x = 20; x >= 0; x--){
            for(int y = 20; y >= 0; y--){
                code[x][y] = new Pixel(false, 1); // Code Array mit weißen Pixeln füllen

            }
        }

        if(masktype == -2){ // wenn man Mask Type -2 verwendet wird ein QR-Code mit jedem Mask Pattern generiert
            for(int i = 0; i <= 7; i++){
                masktype = i;
                formatInfoRaw = text.length() <= 14 ? text.length() <= 11 ? text.length() <= 7 ? "10" : "11" : "00" : "01"; // je nach Länge des Textes wird das Error Correction Level angegeben
                formatInfoRaw = formatInfoRaw.concat(dezinbin(masktype, 2));
                generate(message, masktype);
            }
        }else if(masktype < 8 && masktype >= 0){ // normale anwendung von Pattern
            formatInfoRaw = text.length() <= 14 ? text.length() <= 11 ? text.length() <= 7 ? "10" : "11" : "00" : "01";
            formatInfoRaw = formatInfoRaw.concat(dezinbin(masktype, 2));
            generate(message, masktype);
        }else{
            System.out.println("Dieses Mask Patten existiert nicht");
            return;
        }

    }

    public void generate(String msg, int typ){

        datatobyte(msg); // Daten wie Wortlänge oder Wort an sich in Byte Array
        createprepatterns(code); // Patterns, welche statisch sind werden erstellt
        formatinfotofile(code); // Format Informationen werden erstellt
        bytetofile(inserthalfbyteup(20, 20, enctype, code), 0, bytes[0], code); // Daten werden in den Code eingefügt
        applymask(code, typ); // Maske wird angewandt
        print(code); // QR-Code wird als Datei gespeichert
    }

    private void datatobyte(String msg){ // Daten wie Wortlänge oder Wort an sich in Byte Array

        for(int i = 0; i < 8; i++){
            bytes[0][i] = Integer.parseInt(String.valueOf(dezinbin(msg.length(), 1).charAt(i))) == 0 ? new Pixel(false, 1) : new Pixel(true, 1); // Nachrichtenlänge wird in Byte eingefügt
        }

        for(int i = 0; i < msg.length(); i++){
            for(int k = 0; k < 8; k++){
                bytes[i+1][k] = Integer.parseInt(String.valueOf(dezinbin(msg.charAt(i), 1).charAt(k))) == 0 ? new Pixel(false, 1) : new Pixel(true, 1); // Buchstaben werden eigefügt
            }
        }

        for(int i = message.length()+1; i < 17; i++){
            for(int k = 0; k < 8; k++){
                bytes[i][k] = new Pixel(false, 0); // Leere Indizes Werden mit leeren pixeln gefüllt
            }
        }

    }

    private void createprepatterns(Pixel[][] arr){ // Statische Patterns werden eingefügt
        arr[8][13] = new Pixel(true, 2);    // Dark Module

        for(int x = 8; x < 13; x++){    // Top Timing Pattern
            if(x % 2 == 0){
                arr[x][6] = new Pixel(true, 3);
            }else{
                arr[x][6] = new Pixel(false, 3);
            }
        }

        for(int y = 8; y < 13; y++){    // Left Timing Pattern
            if(y % 2 == 0){
                arr[6][y] = new Pixel(true, 3);
            }else{
                arr[6][y] = new Pixel(false, 3);
            }
        }

        for(int x = 0; x < 21; x++){
            for(int y = 0; y < 21; y++){
                if((x <= 6 && y <= 6) || (x >= 14 && y <= 6) || (x <= 6 && y >= 14)){
                    arr[x][y] = new Pixel(false, 4); //weiße Bereiche in den Ecken
                }
            }
        }

        for(int x = 0; x < 21; x++){
            for(int y = 0; y < 21; y++){
                if(((x > 1 && x < 5) && (y > 1 && y < 5)) || ((x > 15 && x < 19) && (y > 1 && y < 5)) || ((x > 1 && x < 5) && (y > 15 && y < 19))){
                    arr[x][y] = new Pixel(true, 4);     // Middle of alignment pattern
                }
                
                if(((x < 7 || x > 13) && (y == 0 || y == 6)) || ((y < 7 || y > 13) && (x == 0 || x == 6)) || ((x < 7) && (y == 14 || y == 20)) || ((y < 7) && (x == 14 || x == 20))){
                    arr[x][y] = new Pixel(true, 4);     // Outside of Alignment pattern
                }
                
                if((x == 7 && (y < 8 || y > 12)) || (y == 7 && (x < 8 || x > 12)) || (x == 13 && y < 8) || (y == 13 && x < 8)){
                    arr[x][y] = new Pixel(false, 5);    // Seperators
                }
            }
        }
    }

    private void bytetofile(String tmp, int pos, Pixel[] data, Pixel[][] arr){ // Daten werden in den Code eingefügt

        String[] temp = new String[3];
        temp = tmp.split("-"); // temp wird in ein Array aufgeteilt duch trennstrich -

        int[] dat = new int[3];

        for (int i = 0; i < temp.length; i++) {
            dat[i] = Integer.parseInt(temp[i]); // temp wird als int in dat gespeichert
        }

        if(pos > message.length()){ // wenn ende der nachricht erreicht wird
            if(dat[2] == 4){ // wenn es nach links unten gegangen ist
                inserthalfbyteup(dat[0]+1, dat[1]-1, msgend, arr);
            }
            else if(dat[2] == 3){ // wenn es nach links oben gegangen ist
                inserthalfbytedown(dat[0]+1, dat[1]+1, msgend, arr);
            }
            else if(dat[2] == 2){ // wenn es nach unten gegangen ist
                if(arr[dat[0]][dat[1]+1].getType() == 3){ // falls ein Timing Pattern im weg sein sollte
                    inserthalfbytedown(dat[0]+1, dat[1]+2, msgend, arr);
                }
                else{
                    inserthalfbytedown(dat[0]+1, dat[1]+1, msgend, arr);
                }
            }
            else if(dat[2] == 1){ // wenn es nach oben gegangen ist
                if(dat[1]-3 < 0){ // falls Message end außerhalb des Array geraten sollte
                    inserthalfbyteup(dat[0]+1, dat[1]-1, msgend, arr);
                }
                else if(arr[dat[0]][dat[1]-1].getType() == 3){ // falls ein Timing Pattern im weg sein sollte
                    inserthalfbyteup(dat[0]+1, dat[1]-2, msgend, arr);
                }
                else{
                    inserthalfbyteup(dat[0]+1, dat[1]-1, msgend, arr);
                }
                
            }
            
            return;
        }

        if(dat[2] == 1){ // wenn zuletzt nach oben gegangen sein sollte

            // abfragen die bestimmte Fälle abdecken
            
            if(dat[1]-3 < 0){
                bytetofile(golefttop(dat[0]+1, dat[1]-1, data, arr), pos+=1, bytes[pos], arr);
            }
            else if((arr[dat[0]][dat[1]-1].getType() == 2)){
                bytetofile(godown(dat[0]-1, dat[1], data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]+1][dat[1]-1].getType() == 3){
                bytetofile(goup(dat[0]+1, dat[1]-2, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]+1][dat[1]-3].getType() == 1){
                bytetofile(goup(dat[0]+1, dat[1]-1, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]-1][dat[1]-3].getType() == 2){
                bytetofile(golefttop(dat[0]+1, dat[1]-1, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]+1][dat[1]-3].getType() == 3){
                bytetofile(goupinterrupt(dat[0]+1, dat[1]-1, data, arr), pos+=1, bytes[pos], arr);
            }
        }
        else if(dat[2] == 2){ // wenn zuletzt nach unten gegangen wurde

            // abfragen die bestimmte Fälle abdecken
            
            if(dat[1]+1 >= 21){
                bytetofile(goup(dat[0]-1, dat[1], data, arr), pos+=1, bytes[pos], arr);
            }
            else if(dat[1]+3 >= 21 && arr[dat[0]][dat[1]+1].getType() == 1){
                bytetofile(goleftbottom(dat[0]+1, dat[1]+1, data, arr), pos+=1, bytes[pos], arr);
            }
            else if((dat[1]+3 > 20) || (arr[dat[0]][dat[1]+1].getType() == 5)){
                bytetofile(goup(dat[0]-1, dat[1], data, arr), pos+=1, bytes[pos], arr);
            }
            else if(dat[0] == 9 && dat[1] == 20){
                bytetofile(goup(8, 12, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]][dat[1]+1].getType() == 3){
                bytetofile(godown(dat[0]+1, dat[1]+2, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]+1][dat[1]+3].getType() == 1){
                bytetofile(godown(dat[0]+1, dat[1]+1, data, arr), pos+=1, bytes[pos], arr);
            }
            else if(arr[dat[0]+1][dat[1]+3].getType() == 3){
                bytetofile(godowninterrupt(dat[0]+1, dat[1]+1, data, arr), pos+=1, bytes[pos], arr);
            }
            
        }
        else if(dat[2] == 3){ // wenn zuletzt nach links oben gegangen wurde
            bytetofile(godown(dat[0]+1, dat[1]+1, data, arr), pos+=1, bytes[pos], arr);
        }
        else if(dat[2] == 4){ // wen zuletzt nach rechts oben gegangen wurde
            bytetofile(goup(dat[0]+1, dat[1]-1, data, arr), pos+=1, bytes[pos], arr);
        }

    }

    private void formatinfotofile(Pixel[][] arr){ // Format Informationen werden erstellt

        String info = searchFormatInfo(formatInfoRaw); // Holt sich die richtigen Format Informationen mit Error Correction

        for(int x = 0; x < 6; x++){
            arr[x][8] = Integer.parseInt(String.valueOf(info.charAt(x))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
        }

        int tmp = 9;

        for(int y = 5; y >= 0; y--){
            arr[8][y] = Integer.parseInt(String.valueOf(info.charAt(tmp))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
            tmp++;
        }

        arr[7][8] = Integer.parseInt(String.valueOf(info.charAt(6))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
        arr[8][8] = Integer.parseInt(String.valueOf(info.charAt(7))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
        arr[8][7] = Integer.parseInt(String.valueOf(info.charAt(8))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);

        tmp = 0;

        for(int y = 20; y >= 14; y--){
            arr[8][y] = Integer.parseInt(String.valueOf(info.charAt(tmp))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
            tmp++;
        }

        for(int x = 13; x <= 20; x++){
            arr[x][8] = Integer.parseInt(String.valueOf(info.charAt(x-6))) == 1 ? new Pixel(true ,2) : new Pixel(false, 2);
        }

        // bei allem weiterem oben werden die Bits in den QR-Code geschrieben
    }

    private void applymask(Pixel[][] arr, int type){ // Maske wird angewandt
        if(type == 0){ // Maske 0
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if((x+y)%2 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }
        
        if(type == 1){ // Maske 1
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if(y % 2 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 2){ // Maske 2
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if(x % 3 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 3){ // Maske 3
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if((x+y)%3 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 4){ // Maske 4
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if((Math.floor(y/2) + Math.ceil(x/3)) % 2 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 5){ // Maske 5
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if((x*y)%2+(x*y)%3 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 6){ // Maske 6
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if(((x*y)%3+x*y)%2 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }

        if(type == 7){ // Maske 7
            for(int x = 0; x < 21; x++){
                for(int y = 0; y < 21; y++){
                    if(((x*y)%3+x+y)%2 == 0 && (arr[x][y].getType() == 1 || arr[x][y].getType() == 6 || arr[x][y].getType() == 7)){
                        arr[x][y].flip();
                    }
                }
            }
        }
    }

    private void print(Pixel[][] arr){ // QR-Code wird als Datei gespeichert

        BufferedImage image = new BufferedImage(21, 21, BufferedImage.TYPE_INT_ARGB); // erstellt ein neues Bild

        for(int x = 0; x <= 20; x++){ // Geht Array Code durch und setzt die jeweiligen Pixel in das Bild
            for(int y = 0; y <= 20; y++){                
                if(arr[x][y].getBit() == true){
                    image.setRGB(x, y, Color.BLACK.getRGB());
                }else if(arr[x][y].getBit() == false){
                    image.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        File dir = new File("../Generator/output/"); // wenn nicht vorhanden wird ein neuer Ordner mit dem Namen Output erstellt
        if(!dir.exists()){
            new File("../Generator/output/").mkdir();
        }

        try{ // Erstellt ein neues Bild unter output mit dem Namen Wort-Maskennummer
            File output = new File("../Generator/output/" + message + "-" + masktype +".png");
            ImageIO.write(image, "png", output);
        } catch (IOException e){
            System.out.println(e);
        }
    }

    private String dezinbin(int zahl, int type){ // wandelt je nach Type dezimalzahlen in Binärzahlen mit entweder 8 bits oder 5 bits

        String out = "";

        out = Integer.toString(zahl%2);
        int temp = zahl/2;

        while (temp >= 1) {
            out = Integer.toString(temp%2) + out;
            temp /= 2;
        }

        if(type == 1){
            for(int i = out.length(); i < 8; i++){
                out = 0 + out;
            }
        }else if(type == 2){
            for(int i = out.length(); i < 3; i++){
                out = 0 + out;
            }
        }
        
        return out;
    }

    private String searchFormatInfo(String key){ // sucht die Formatinformationen
        return jsonreader.search(key);
    }

    /*
     * Folgend die Methoden damit der Generator die Zeichen auch in den Code einfügen kann
     * goup wird beispielsweise verwendet wenn ein Byte nach oben geschrieben werden soll
     * Die methoden mit Halfbyte sind für Encoding Type und Message end
     */


    private String goup(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte nach oben geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y-1] = data[2];
        arr[x-1][y-1] = data[3];
        arr[x][y-2] = data[4];
        arr[x-1][y-2] = data[5];
        arr[x][y-3] = data[6];
        arr[x-1][y-3] = data[7];

        return (x-1) + "-" + (y-3) + "-" + 1;
    }

    private String golefttop(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte links oben herum geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y-1] = data[2];
        arr[x-1][y-1] = data[3];
        arr[x-2][y-1] = data[4];
        arr[x-3][y-1] = data[5];
        arr[x-2][y] = data[6];
        arr[x-3][y] = data[7];

        return (x-3) + "-" + y + "-" + 3;
    }

    private String godown(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte nach unten geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y+1] = data[2];
        arr[x-1][y+1] = data[3];
        arr[x][y+2] = data[4];
        arr[x-1][y+2] = data[5];
        arr[x][y+3] = data[6];
        arr[x-1][y+3] = data[7];

        return (x-1) + "-" + (y+3) + "-" + 2;
    }

    private String goleftbottom(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte nach links unten herum geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y+1] = data[2];
        arr[x-1][y+1] = data[3];
        arr[x-2][y+1] = data[4];
        arr[x-3][y+1] = data[5];
        arr[x-2][y] = data[6];
        arr[x-3][y] = data[7];

        return (x-3) + "-" + y + "-" + 4;
    }

    private String godowninterrupt(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte nach unten geschrieben werden soll aber ein Timing Pattern im weg ist
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y+1] = data[2];
        arr[x-1][y+1] = data[3];
        arr[x][y+3] = data[4];
        arr[x-1][y+3] = data[5];
        arr[x][y+4] = data[6];
        arr[x-1][y+4] = data[7];

        return (x-1) + "-" + (y+4) + "-" + 2;
    }

    private String goupinterrupt(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein byte nach oben geschrieben werden soll aber ein Timing Pattern im weg ist
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y-1] = data[2];
        arr[x-1][y-1] = data[3];
        arr[x][y-3] = data[4];
        arr[x-1][y-3] = data[5];
        arr[x][y-4] = data[6];
        arr[x-1][y-4] = data[7];

        return (x-1) + "-" + (y-4) + "-" + 1;
    }

    private String inserthalfbyteup(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein halfbyte nach oben geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y-1] = data[2];
        arr[x-1][y-1] = data[3];

        return (x-1) + "-" + (y-1) + "-" + "1";
    }

    private String inserthalfbytedown(int x, int y, Pixel[] data, Pixel[][] arr){ // wird verwendet wenn ein halfbyte nach unten geschrieben werden soll
        arr[x][y] = data[0];
        arr[x-1][y] = data[1];
        arr[x][y+1] = data[2];
        arr[x-1][y+1] = data[3];

        return (x-1) + "-" + (y+1) + "-" + "2";
    }

}