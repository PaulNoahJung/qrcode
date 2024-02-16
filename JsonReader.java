import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class JsonReader
{
    private Gson gson = new Gson();
    public JsonReader(){}

    public String search(String key) {

        String stringResponse = null;

        try {
            BufferedReader br = new BufferedReader(new FileReader("formatinformation.json"));
            JsonObject testOBJ = gson.fromJson(br, JsonObject.class);
            stringResponse = testOBJ.get(key).toString();

        } catch (IOException ignored) {

        }

        return stringResponse;
    }

}