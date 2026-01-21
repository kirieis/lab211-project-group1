/**
 * Loại bỏ dữ liệu rác:
 * - name rỗng
 * - unit rỗng
 * - price không phải số hoặc <= 0
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DataCleaner {

    public static void main(String[] args) throws Exception {

        BufferedReader br = new BufferedReader(
                new FileReader("raw_medicine_data.csv"));
        BufferedWriter bw = new BufferedWriter(
                new FileWriter("clean_medicine_data.csv"));

        String line;
        bw.write("medicine_id,name,unit,price\n");
        br.readLine(); // bỏ header

        int total = 0, clean = 0;

        while ((line = br.readLine()) != null) {
            total++;
            String[] p = line.split(",");

            if (p.length != 4) continue;
            if (p[1].isEmpty() || p[2].isEmpty()) continue;

            try {
                double price = Double.parseDouble(p[3]);
                if (price <= 0) continue;
            } catch (Exception e) {
                continue;
            }

            bw.write(line);
            bw.newLine();
            clean++;
        }

        br.close();
        bw.close();

        System.out.println("Total: " + total);
        System.out.println("Clean records: " + clean);
    }
}
