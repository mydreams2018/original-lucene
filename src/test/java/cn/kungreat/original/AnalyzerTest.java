package cn.kungreat.original;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalyzerTest {

    @Test
    public void analyzer(){
        Analyzer analyzer = new IKAnalyzer();
        excuteAnalyzer(analyzer,"中华是我家,我是小骊哥");
    }


    public void excuteAnalyzer(Analyzer analyzer,String text){
        StringReader reader = new StringReader(text);
        TokenStream tokenStream = analyzer.tokenStream("name", reader);
        try {
            tokenStream.reset();
            CharTermAttribute charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
            while (tokenStream.incrementToken()){
                System.out.println(charTermAttribute.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(analyzer != null){
                analyzer.close();
            }
            if(tokenStream != null){
                try {
                    tokenStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
