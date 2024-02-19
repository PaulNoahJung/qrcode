import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JsonReader
{
    private Gson gson = new Gson(); // Neue Json Klasse wird erstellt
    public JsonReader(){}

    public String search(String key) { // Nach Format Information Suchen

        String stringResponse = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader("formatinformation.json")); // neuer Reader wird erstellt, der die .json datei liest
            JsonObject testOBJ = gson.fromJson(br, JsonObject.class); // Neues Objekt wird erstllt
            stringResponse = testOBJ.get(key).toString(); // antwort wird in einen string gepackt

            stringResponse = stringResponse.substring(1, stringResponse.length() -1); // zieht die anführungszeichen des Strings ab

        } catch (IOException ignored) { // IOExeption wird abgefangen
            
        }

        return stringResponse;
    }

}