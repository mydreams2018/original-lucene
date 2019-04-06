package cn.kungreat.original;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class count {

    @Value("${lucene.path}")
    private String lucenePath;
    //中文分词 默认不使用 细分词  可以传入 true 使用细分词
    Analyzer analyzer = new IKAnalyzer(true);//中文分词
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    @Before
    public void init(){
        try {
            //索引存放的位置，设置在当前目录中
            directory = FSDirectory.open(Paths.get(lucenePath));
            //创建索引的读取器
            indexReader = DirectoryReader.open(directory);
            //创建一个索引的查找器，来检索索引库
            indexSearcher = new IndexSearcher(indexReader);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @After
    public void close(){
        if(indexReader != null){
            try {
                indexReader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (directory != null){
            try {
                directory.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void creat(){
        //创建索引写入配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //创建索引写入对象
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            //创建Document对象，存储索引
            Document doc = new Document();
            FieldType fieldType = new FieldType();
            fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            fieldType.setStored(true);
            fieldType.setStoreTermVectors(true);
            fieldType.setTokenized(true);
            //将字段加入到doc中
            doc.add(new Field("content", "java 是专为大规模数据处理而设计的快速通用的计算引擎",fieldType));
            indexWriter.addDocument(doc);
            indexWriter.commit();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(indexWriter != null ){
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void count(){
        try {
            Terms content = indexReader.getTermVector(5, "content");
            TermsEnum iterator = content.iterator();
            BytesRef bytesRef = iterator.next();
            while (bytesRef != null){
                System.out.println(bytesRef.utf8ToString());  //词元
                System.out.println(iterator.totalTermFreq()); //数量
                bytesRef =  iterator.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
