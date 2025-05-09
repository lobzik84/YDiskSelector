package org.lobzik.yselector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
public class YDiskSelector {


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // Получаем путь к jar-файлу
            String jarPath = new File(YDiskSelector.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();

            File propertiesFile = new File(jarPath, "YDiskSelector.properties");

            if (!propertiesFile.exists()) {
                System.err.println("Файл YDiskSelector.properties не найден в: " + jarPath);
                return;
            }

            Properties props = new Properties();
            // Загружаем с учетом UTF-8
            try (InputStreamReader reader = new InputStreamReader(
                    new FileInputStream(propertiesFile), StandardCharsets.UTF_8)) {
                props.load(reader);
            }

            /*System.out.println("Содержимое файла YDiskSelector.properties:");
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                System.out.printf("%s = %s%n", key, value);
            }*/
            FrameWindow frame = new FrameWindow("Yandex Disk Selector v1.2  " + props.getProperty("ROOT_FOLDER"), props);
            frame.setVisible(true);

        } catch (Exception e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
            e.printStackTrace();
        }

    }


}
