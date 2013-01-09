create table street_addresses (
  ID varchar(8),
  POSTCODE varchar(8),
  GEMEENTENAAM varchar(128),
  STRAATNAAM varchar(128),
  HUISNUMMER varchar(8),
  BUSNUMMER varchar(8),
  UITREIKINGSKANTOOR varchar(128),
  GEMEENTENAAMCORRECTIE varchar(128),
  STRAATNAAMCORRECTIE varchar(128),
  HUISNUMMERCORRECTIE varchar(8),
  BUSNUMMERCORRECTIE varchar(8),
  STATUS varchar(8),
  PDPID varchar(8),
  PDPSUFFIX varchar(8),
  ERRORCODE varchar(8),
  ERRORMESSAGE varchar(256),
  ABO2000 varchar(128),
  BS2000 varchar(128),
  CREATED_ON varchar(16),
  LAST_UPDATED_ON varchar(16)
);