package com.example.ddd;


import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;

public class JooqCodeGenerator {

    public static void main(String[] args) throws Exception {
        // JOOQ代码生成配置 - PostgreSQL
        Configuration configuration = new Configuration()
                .withJdbc(new Jdbc()
                        .withDriver("org.postgresql.Driver")
                        .withUrl("jdbc:postgresql://localhost:5432/agent")
                        .withUser("postgres")
                        .withPassword("postgres")
                )
                .withGenerator(new Generator()
                        .withDatabase(new Database()
                                .withName("org.jooq.meta.postgres.PostgresDatabase")
                                .withIncludes(".*")
                                .withExcludes("")
                                .withInputSchema("public")
                        )
                        .withTarget(new Target()
                                .withPackageName("com.example.jooq")
                                .withDirectory("src/main/java")
                        )
                );
        // 执行代码生成
        GenerationTool.generate(configuration);
        System.out.println("JOOQ代码生成完成！");
    }
}
