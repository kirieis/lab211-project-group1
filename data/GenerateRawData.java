/**
 * Sinh 10.000 dữ liệu thuốc
 * 5–10% dữ liệu bị lỗi CỐ Ý:
 * - name rỗng
 * - giá âm
 * - giá không phải số
 * - unit null
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;

public class GenerateRawData {

    public static void main(String[] args) throws Exception {
        BufferedWriter bw = new BufferedWriter(
                new FileWriter("raw_medicine_data.csv"));

        bw.write("medicine_id,name,unit,price\n");

        Random rand = new Random();
        int TOTAL = 10000;

        for (int i = 1; i <= TOTAL; i++) {
            String id = "M" + String.format("%05d", i);
            String name = "Medicine_" + i;
            String unit = "box";
            String price = String.valueOf(10000 + rand.nextInt(90000));

            int errorChance = rand.nextInt(100); // 0–99

            // 7% dữ liệu lỗi
            if (errorChance < 7) {
                switch (errorChance % 4) {
                    case 0 -> name = "";              // thiếu tên
                    case 1 -> price = "-5000";        // giá âm
                    case 2 -> price = "abc";          // sai định dạng
                    case 3 -> unit = "";              // thiếu unit
                }
            }

            bw.write(String.join(",", id, name, unit, price));
            bw.newLine();
        }

        bw.close();
        System.out.println("Generated 10,000 raw records with dirty data.");
    }
}
