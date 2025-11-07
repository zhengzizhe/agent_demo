create table public.agent
(
    id          bigserial
        primary key,
    name        varchar(255) not null,
    description text,
    status      varchar(50) default 'ACTIVE'::character varying,
    created_at  bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    updated_at  bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint
);

comment on table public.agent is 'Agent表';

comment on column public.agent.id is '主键ID';

comment on column public.agent.name is 'Agent名称';

comment on column public.agent.description is 'Agent描述';

comment on column public.agent.status is '状态：ACTIVE, INACTIVE';

comment on column public.agent.created_at is '创建时间（时间戳，秒）';

comment on column public.agent.updated_at is '更新时间（时间戳，秒）';

alter table public.agent
    owner to postgres;

create table public.model
(
    id           bigserial
        primary key,
    name         varchar(255) not null,
    provider     varchar(100) not null,
    model_type   varchar(100),
    api_endpoint varchar(500),
    api_key      varchar(500),
    max_tokens   integer,
    temperature  double precision default 0.7,
    status       varchar(50)      default 'ACTIVE'::character varying,
    created_at   bigint           default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    updated_at   bigint           default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint
);

comment on table public.model is 'Model表';

comment on column public.model.id is '主键ID';

comment on column public.model.name is 'Model名称';

comment on column public.model.provider is '提供商：OPENAI, ANTHROPIC, OLLAMA等';

comment on column public.model.model_type is '模型类型';

comment on column public.model.api_endpoint is 'API端点';

comment on column public.model.api_key is 'API密钥（加密存储）';

comment on column public.model.max_tokens is '最大token数';

comment on column public.model.temperature is '温度参数';

comment on column public.model.status is '状态：ACTIVE, INACTIVE';

comment on column public.model.created_at is '创建时间（时间戳，秒）';

comment on column public.model.updated_at is '更新时间（时间戳，秒）';

alter table public.model
    owner to postgres;

create table public.mcp
(
    id         bigserial
        primary key,
    name       varchar(255) not null,
    type       varchar(100) not null,
    endpoint   varchar(500),
    config     jsonb,
    status     varchar(50) default 'ACTIVE'::character varying,
    created_at bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    updated_at bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint
);

comment on table public.mcp is 'MCP表（Model Context Protocol）';

comment on column public.mcp.id is '主键ID';

comment on column public.mcp.name is 'MCP名称';

comment on column public.mcp.type is 'MCP类型';

comment on column public.mcp.endpoint is 'MCP端点';

comment on column public.mcp.config is 'MCP配置信息（JSON格式）';

comment on column public.mcp.status is '状态：ACTIVE, INACTIVE';

comment on column public.mcp.created_at is '创建时间（时间戳，秒）';

comment on column public.mcp.updated_at is '更新时间（时间戳，秒）';

alter table public.mcp
    owner to postgres;

create table public.rag
(
    id                bigserial
        primary key,
    name              varchar(255) not null,
    vector_store_type varchar(100),
    embedding_model   varchar(255),
    chunk_size        integer      default 1000,
    chunk_overlap     integer      default 200,
    database_type     varchar(50)  default 'POSTGRESQL'::character varying,
    database_host     varchar(255),
    database_port     integer      default 5432,
    database_name     varchar(255),
    database_user     varchar(255),
    database_password varchar(255),
    table_name        varchar(255) default 'vector_document'::character varying,
    dimension         integer      default 1536,
    use_index         boolean      default true,
    index_list_size   integer      default 1000,
    config            jsonb,
    status            varchar(50)  default 'ACTIVE'::character varying,
    created_at        bigint       default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    updated_at        bigint       default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint
);

comment on table public.rag is 'RAG表（Retrieval-Augmented Generation）';

comment on column public.rag.id is '主键ID';

comment on column public.rag.name is 'RAG名称';

comment on column public.rag.vector_store_type is '向量存储类型';

comment on column public.rag.embedding_model is '嵌入模型';

comment on column public.rag.chunk_size is '分块大小';

comment on column public.rag.chunk_overlap is '分块重叠大小';

comment on column public.rag.database_type is '数据库类型：POSTGRESQL, MYSQL等';

comment on column public.rag.database_host is '数据库主机地址';

comment on column public.rag.database_port is '数据库端口';

comment on column public.rag.database_name is '数据库名称';

comment on column public.rag.database_user is '数据库用户名';

comment on column public.rag.database_password is '数据库密码';

comment on column public.rag.table_name is '向量存储表名';

comment on column public.rag.dimension is '向量维度';

comment on column public.rag.use_index is '是否使用索引';

comment on column public.rag.index_list_size is '索引列表大小';

comment on column public.rag.config is 'RAG配置信息（JSON格式，扩展配置）';

comment on column public.rag.status is '状态：ACTIVE, INACTIVE';

comment on column public.rag.created_at is '创建时间（时间戳，秒）';

comment on column public.rag.updated_at is '更新时间（时间戳，秒）';

alter table public.rag
    owner to postgres;

create table public.client
(
    id            bigserial
        primary key,
    name          varchar(255) not null,
    description   text,
    status        varchar(50) default 'ACTIVE'::character varying,
    created_at    bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    updated_at    bigint      default (EXTRACT(epoch FROM CURRENT_TIMESTAMP))::bigint,
    system_prompt varchar(255)
);

comment on table public.client is 'Client表';

comment on column public.client.id is '主键ID';

comment on column public.client.name is 'Client名称';

comment on column public.client.description is 'Client描述';

comment on column public.client.status is '状态：ACTIVE, INACTIVE';

comment on column public.client.created_at is '创建时间（时间戳，秒）';

comment on column public.client.updated_at is '更新时间（时间戳，秒）';

alter table public.client
    owner to postgres;

create table public.agent_client
(
    agent_id  bigint not null
        constraint fk_agent_client_agent
            references public.agent
            on delete cascade,
    client_id bigint not null
        constraint fk_agent_client_client
            references public.client
            on delete cascade,
    constraint uk_agent_client_unique
        primary key (agent_id, client_id)
);

comment on table public.agent_client is 'Agent和Client的关联表';

comment on column public.agent_client.agent_id is 'Agent ID';

comment on column public.agent_client.client_id is 'Client ID';

alter table public.agent_client
    owner to postgres;

create table public.client_model
(
    client_id bigint not null
        constraint fk_client_model_client
            references public.client
            on delete cascade,
    model_id  bigint not null
        constraint fk_client_model_model
            references public.model
            on delete cascade,
    constraint uk_client_model_unique
        primary key (client_id, model_id)
);

comment on table public.client_model is 'Client和Model的关联表';

comment on column public.client_model.client_id is 'Client ID';

comment on column public.client_model.model_id is 'Model ID';

alter table public.client_model
    owner to postgres;

create table public.client_rag
(
    client_id bigint not null
        constraint fk_client_rag_client
            references public.client
            on delete cascade,
    rag_id    bigint not null
        constraint fk_client_rag_rag
            references public.rag
            on delete cascade,
    constraint uk_client_rag_unique
        primary key (client_id, rag_id)
);

comment on table public.client_rag is 'Client和RAG的关联表';

comment on column public.client_rag.client_id is 'Client ID';

comment on column public.client_rag.rag_id is 'RAG ID';

alter table public.client_rag
    owner to postgres;

create table public.client_mcp
(
    client_id bigint not null,
    mcp_id    bigint not null,
    constraint uk_client_mcp_unique
        primary key (client_id, mcp_id)
);

comment on table public.client_mcp is 'Client和MCP的关联表';

comment on column public.client_mcp.client_id is 'Client ID';

comment on column public.client_mcp.mcp_id is 'MCP ID';

alter table public.client_mcp
    owner to postgres;

create table public.vector_document
(
    embedding_id uuid not null
        primary key,
    embedding    vector(1536),
    text         text,
    metadata     json
);

alter table public.vector_document
    owner to postgres;

create index vector_document_ivfflat_index
    on public.vector_document using ivfflat (embedding public.vector_cosine_ops);

