package com.program.spring.ai.config;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class VectorLoader {



    @Value("classpath:/Constitution_of_India.pdf")
    private Resource pdfResource;

    private final VectorStore vectorStore;
    private JdbcClient jdbcClient;


    public VectorLoader(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @PostConstruct
    public void init(){
        Integer count = jdbcClient
                .sql("select COUNT(*) from vector_store")
                .query(Integer.class)
                .single();
        System.out.println("No of Documents in PG vector store = "+count);

        if(count == 0){
            System.out.println("Initializing pg vector store load");

            PdfDocumentReaderConfig config = PdfDocumentReaderConfig
                    .builder()
                    .withPagesPerDocument(1)
                    .build();

            PagePdfDocumentReader reader = new PagePdfDocumentReader(pdfResource,config);
            var textSplitter = new TokenTextSplitter();

            vectorStore.accept(textSplitter.apply(reader.get()));

            System.out.println("Vector store initialized successfully.");
        }
    }

}
