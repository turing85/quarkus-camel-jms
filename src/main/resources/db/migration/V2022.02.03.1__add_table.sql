CREATE SEQUENCE data_seq_id;

CREATE TABLE public.data (
  id BIGINT CONSTRAINT application_pk_id PRIMARY KEY DEFAULT nextval('data_seq_id'),
  data VARCHAR(255) NOT NULL
);

ALTER SEQUENCE data_seq_id OWNED BY public.data.id;