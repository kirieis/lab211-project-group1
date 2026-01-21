package simulator;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

public class POSSimulator {

    public static void main(String[] args) throws Exception {
        Random r = new Random();

        while (true) {
            URL url = new URL("http://localhost:8080/core_app/api/sell");
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("POST");
            c.setDoOutput(true);

            String data = "medicineId=M00001&quantity=" + (r.nextInt(3)+1);
            c.getOutputStream().write(data.getBytes());

            Thread.sleep(200);
        }
    }
}
