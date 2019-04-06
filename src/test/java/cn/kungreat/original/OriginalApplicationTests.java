package cn.kungreat.original;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ClassUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OriginalApplicationTests {

    @Value("${lucene.path}")
    private String lucenePath;
    //中文分词 默认不使用 细分词  可以传入 true 使用细分词
    Analyzer analyzer = new IKAnalyzer(false);//中文分词
    private Directory directory;
    private IndexReader indexReader;
    private IndexSearcher indexSearcher;

    @Test
    public void contextLoads() {
        // 获得当前运行项目路径
        String path = ClassUtils.getDefaultClassLoader().getResource("").getPath();
        System.out.println(path);
        System.out.println(System.lineSeparator());
    }
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
    
    /*
    * StringField  TextField 这种自带的是已经定意好了 一定的类型结构 
    * 我们也可以使用 自定意的类型结构
    *
         FieldType fieldType = new FieldType();
         fieldType.setIndexOptions(IndexOptions.DOCS);
         fieldType.setStored(true);
        new Field("name","value",fieldType);

        IndexOptions.DOCS  只有文档会被索引，词频和位置都会被省略
        DOCS_AND_FREQS  文档和词频被索引，位置省略
        DOCS_AND_FREQS_AND_POSITIONS  文档 词频 位置都被索引
        DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS  除了文档和词频位置还有偏移量也会被索引
        NONE   不索引

        stored:     是否存储。
        tokenized:  是否分词。
        storeTermVectors： 是否存储向量信息。
        storeTermVectorOffsets    向量位移
        storeTermVectorPositions  向量偏移
        storeTermVectorPayloads： 是否存储Payloads信息。
        omitNorms：  是否存储norm信息，用于评分Boost值和会处理。

    */
    
    @Test
    public void creat(){
        //在 6.6 以上版本中 version 不再是必要的
        Version version = Version.LUCENE_7_1_0;
        //创建索引写入配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        //创建索引写入对象
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            //创建Document对象，存储索引
            Document doc = new Document();
            //将字段加入到doc中
            doc.add(new StringField("title", "java", Field.Store.YES));
            doc.add(new TextField("content", "java 是专为大规模数据处理而设计的快速通用的计算引擎", Field.Store.YES));
            // DocValues 类型不分词  会创建一个排序的表  用来做排序 很快
            doc.add(new SortedDocValuesField("id",new BytesRef("180")));
            doc.add(new StringField("id", "180",Field.Store.YES));

            doc.add(new IntPoint("bk", 136));
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

    @Test           //不分词
    public void termQueryTest() throws IOException {
        TermQuery query = new TermQuery(new Term("title", "java"));
        //执行查询，并打印查询到的记录数
        executeQuery(query);
    }

    @Test           //分词
    public void queryParser(){
        QueryParser queryParser = new QueryParser("title", analyzer);
        queryParser.setDefaultOperator(QueryParser.Operator.AND);  // 查询值不分词
        try {
            Query query = queryParser.parse("c");
            executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test           //多field 分词
    public void multiFieldQueryParser(){
        String fields[] = {"title","content"};
        MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
        try {
            Query query = queryParser.parse("c");
            executeQuery(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test           //boolQuery query 拼接
    public void boolQuery(){

        try {
            String fields[] = {"title","content"};
            MultiFieldQueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query query1 = queryParser.parse("c");

            Query query2 = new TermQuery(new Term("content","nono"));

            BooleanClause booleanClause1 = new BooleanClause(query1, BooleanClause.Occur.MUST);
            BooleanClause booleanClause2 = new BooleanClause(query2, BooleanClause.Occur.SHOULD);
            BooleanQuery booleanQuery = new BooleanQuery.Builder().add(booleanClause1).add(booleanClause2).build();

            executeQuery(booleanQuery);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test   // 需要保存一个  doc.add(new IntPoint("bk", 136));  用来快速过滤定位 [不会保存在词库 查不到]
    public void rangQuery(){
        Query query = IntPoint.newRangeQuery("bk", 100, 160);
        try {
            executeQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test   //prefixQuery
    public void prefixQuery(){
        Term term = new Term("content", "j");
        PrefixQuery prefixQuery = new PrefixQuery(term);
        try {
            executeQuery(prefixQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void PhraseQuery()  {
        PhraseQuery.Builder builder = new PhraseQuery.Builder();
        builder.add(new Term("content","c"));
        builder.add(new Term("content","original"));
        //设置分词之间的距离   content：c is original
        builder.setSlop(1);
        PhraseQuery phraseQuery = builder.build();
        try {
            executeQuery(phraseQuery);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test  //FuzzyQuery是一种模糊查询，它可以简单地识别两个相近的词语。
    public void fuzzyQuery() {
        Term term = new Term("content", "大规模");
        Query query = new FuzzyQuery(term);
        //执行查询，并打印查询到的记录数
        try {
            executeQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通配符搜索
     * Lucene也提供了通配符的查询，这就是WildcardQuery。
     * 通配符“?”代表1个字符，而“*”则代表0至多个字符。
     */
    @Test
    public void wildcardQuery() {
        Term term = new Term("content", "大?模");
        Query query = new WildcardQuery(term);
        //执行查询，并打印查询到的记录数
        try {
            executeQuery(query);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void highLight(){
        //指定搜索字段和分析器
        QueryParser parser = new QueryParser("content", analyzer);
        //用户输入内容
        Query query = null;
        try {
            query = parser.parse("java");
            // 关键字高亮显示的html标签，需要导入lucene-highlighter-xxx.jar
            QueryScorer queryScorer = new QueryScorer(query,"content");
            SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<span style='color:red'>", "</span>");
            Highlighter highlighter = new Highlighter(simpleHTMLFormatter,queryScorer);
            TopDocs topDocs = indexSearcher.search(query, 10);
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                System.out.println("分数" + scoreDoc.score);
                System.out.println("编号" + scoreDoc.doc);
                //取得对应的文档对象
                Document document = indexSearcher.doc(scoreDoc.doc);
                System.out.println("id：" + document.get("id"));
                System.out.println("title：" + document.get("title"));
                System.out.println("content：" + document.get("content"));
                System.out.println("bk：" + document.get("bk"));
                System.out.println("------------------------------------");
                // 内容增加高亮显示
                TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(document.get("content")));
                SimpleSpanFragmenter simpleSpanFragmenter = new SimpleSpanFragmenter(queryScorer);
                highlighter.setTextFragmenter(simpleSpanFragmenter);
                String content = highlighter.getBestFragment(tokenStream, document.get("content"));
                System.out.println(content);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void executeQuery(Query query) throws IOException {
        TopDocs topDocs = indexSearcher.search(query, 10);
        //打印查询到的记录数
        System.out.println("总共查询到" + topDocs.totalHits + "个文档");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            System.out.println("分数" + scoreDoc.score);
            System.out.println("编号" + scoreDoc.doc);
            //取得对应的文档对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id：" + document.get("id"));
            System.out.println("title：" + document.get("title"));
            System.out.println("content：" + document.get("content"));
            System.out.println("bk：" + document.get("bk"));
        }
    }

    @Test
    public void deleteDocumentsTest() {
        //创建索引写入配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //创建索引写入对象
        IndexWriter indexWriter = null;
        try{
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            // 删除title中含有关键词“Spark”的文档
            long count = indexWriter.deleteDocuments(new Term("title", "Spark"));
            // DeleteDocuments(Term term ...):根据Term来删除单个或多个Document
            // DeleteDocuments(Query query ...):根据Query条件来删除单个或多个Document
            // DeleteAll():删除所有的Document

            //使用IndexWriter进行Document删除操作时，文档并不会立即被删除，而是把这个删除动作缓存起来，
            // 当IndexWriter.Commit()或IndexWriter.Close()时，删除操作才会被真正执行。
            indexWriter.commit();
            System.out.println("删除完成:" + count);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(indexWriter != null){
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 测试更新
     * 实际上就是删除后新增一条
     */
    @Test
    public void updateDocumentTest() {
        //创建索引写入配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        //创建索引写入对象
        IndexWriter indexWriter = null;
        try {
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            Document doc = new Document();
            doc.add(new StringField("title", "c", Field.Store.YES));
            doc.add(new TextField("content", "c is original", Field.Store.YES));
            long count = indexWriter.updateDocument(new Term("id", "150"), doc);
            System.out.println("更新文档:" + count);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(indexWriter != null){
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



}