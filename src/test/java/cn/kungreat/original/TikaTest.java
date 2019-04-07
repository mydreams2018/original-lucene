package cn.kungreat.original;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TikaTest {
    @Value("${tika.path}")
    public String tikaPath;

    @Test
    public void autoParse1(){
        Tika tika = new Tika();
        // 指定目录
        File file = new File(tikaPath);
        if(!file.canExecute()){
            return ;
        }
        //读取目录下所有文件
        File[] files = file.listFiles();
        for(int x=0;x<files.length;x++){
            try {
                System.out.println(tika.parseToString(files[x]));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (TikaException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void autoParse2(){
        Tika tika = new Tika();
        // 指定目录
        File file = new File(tikaPath);
        if(!file.canExecute()){
            return ;
        }
        //读取目录下所有文件
        File[] files = file.listFiles();
        BodyContentHandler bodyContentHandler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        Parser parser = new AutoDetectParser();
        ParseContext context = new ParseContext();
        for(int x=0;x<files.length;x++){
            try {
                // 文件名称
                System.out.println(files[x].getName());
                ((AutoDetectParser) parser).parse(new FileInputStream(files[x]), bodyContentHandler, metadata, context);
                // 文件内容
                System.out.println(bodyContentHandler.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
