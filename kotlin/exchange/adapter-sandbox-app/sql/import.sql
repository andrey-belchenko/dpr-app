--SHOW DATABASES;

--BACKUP DATABASE mrsk_sk11 INTO 'userfile://defaultdb.public.userfiles_$user/backup/mrsk_sk11'
--RESTORE DATABASE mrsk_sk11 FROM LATEST IN 'userfile://defaultdb.public.userfiles_$user/backup/mrsk_sk11' WITH new_db_name = 'mrsk_sk11_backup';
--select * from  public."Transactions"

--
SET TIME ZONE 0;
--select  TIMESTAMPTZ '3000-01-01';
--select timezone('+0',  TIMESTAMP '3000-01-01')
--select  TIMESTAMPTZ '3000-01-01'  AT TIME ZONE '+03:00'


--
--select * from public."Entities" e where "type"='Substation';
--select * from mrsk.public."Entities" e;



truncate table public."Links";
truncate table public."Entities";
delete from  public."Transactions";

ALTER TABLE public."Transactions" ADD COLUMN IF NOT EXISTS entity_iri varchar(1024) NULL;


--ALTER DATABASE mrsk_siberia_load_sk RENAME TO mrsk_siberia_load_sk;

--select TIMESTAMP '3000-01-01+00:00'
--select TIMESTAMPTZ '2016-03-26 10:10:10-05:00'
--select timezone('UTC', TIMESTAMPTZ '3000-01-01+00:00')
insert into public."Transactions" (
		entity_iri,
		meta
	)
select
	distinct
	entity_id as entity_iri,
	'{}' as meta
from
	stg_entities se;



--select count (*) from stg_entities

--insert INTO  "Entities" (
--	iri,
--	type,
--	meta,
--	model,
--	"trId",
--	version,
--	"assertedAt"
--)



drop table stg_entities3;


create table stg_entities3 as
select
	distinct
	concat(a.entity_id) as iri,
	a.type,
	t.meta,
	a.model,
	t.id,
	1 version,
	'1900-01-01'::STRING::timestamptz "assertedAt"
from
	stg_entities a
left join
	public."Transactions" t on a.entity_id=t.entity_iri	;




--select * from stg_entities3 where iri='9de25a34-819e-42d2-a76a-14966ac864a8'

insert INTO  public."Entities" (
	iri,
	type,
	meta,
	model,
	"trId",
	version,
	"assertedAt"
)
select * from stg_entities3;

INSERT INTO public."Links" (
    "fromIri",
    "fromType",
    "toIri" ,
    "toType",
    predicate,
    meta,
    "trId",
    "assertedAt",
    display_name
)
select
	distinct
	concat( ef.entity_id) as "fromIri",
    ef.type as "fromType",
    concat( et.entity_id) "toIri",
    et.type "toType",
    l.name  predicate,
    t.meta,
    t.id as "trId",
	'1900-01-01'::STRING::timestamptz "assertedAt",
	et.model->>'IdentifiedObject.name' as display_name
from stg_links l
	left join public."Transactions" t on l.from_entity_id=t.entity_iri
	left join stg_entities ef on l.from_entity_id=ef.entity_id
	left join stg_entities et on l.to_entity_id=et.entity_id
where et.type is not null;





ALTER TABLE "Transactions" drop column entity_iri;


--select * from "Links"
----------


select count(*) from stg_profile sp
select * from "Links"



drop table if exists stg_profile_pred;

create table stg_profile_pred as
select
	distinct
	sp.range,
	sp.domain,
	sp.predicate
from
	stg_profile sp;

drop table if exists stg_profile_inv;

create table stg_profile_inv as
select
	sp.range,
	sp.predicate,
	max(sp2.predicate) as inv_predicate
from
	stg_profile_pred sp
join stg_profile_pred sp2 on
	sp.domain = sp2.range
	and sp.range = sp2.domain
group by
	sp.domain ,
	sp.range ,
	sp.predicate
having
	count(1)= 1;



--
--select * from stg_profile_inv spi  where predicate='EquipmentContainer.PlaceEquipmentContainer'
--select * from stg_profile where type='Substation'
--select distinct predicate from stg_profile_inv
--select  count(*) from stg_profile_inv
--select *,split_part("name",'.', 1) from  stg_profile where from_type = 'Substation'
--
--
--select * from  stg_profile_inv where from_type = 'Substation'

drop table if exists stg_links_inv_pred;

create table stg_links_inv_pred as
select
	l.*, sp.inv_predicate
from
	public."Links" l
join stg_profile_inv sp on
	l.predicate = sp.predicate




	SET TIME ZONE 0;

--select * from stg_links sl limit 100

drop table if exists stg_links_inv;

--select count(*) from stg_links_inv;

create table stg_links_inv
as
select distinct l."toIri" from_entity_id,l."fromIri"	to_entity_id, l."inv_predicate" as "name" from
stg_links_inv_pred l
left join stg_links_inv_pred l2
on l."fromType" =  l2."toType"
and l2."fromType" =  l."toType"
and l."fromIri"=  l2."toIri"
and l2."fromIri"=  l."toIri"
and l."inv_predicate" = l2."predicate"
where l2."toIri" is null;







INSERT INTO public."Links" (
    "fromIri",
    "fromType",
    "toIri" ,
    "toType",
    predicate,
    meta,
    "trId",
    "assertedAt",
    display_name
)
select
	concat( ef.entity_id) as "fromIri",
    ef.type as "fromType",
    concat( et.entity_id) "toIri",
    et.type "toType",
    l.name  predicate,
    t.meta,
    t.id as "trId",
	'1900-01-01'::STRING::timestamptz "assertedAt",
	et.model->>'IdentifiedObject.name' as display_name
from stg_links_inv l
	left join public."Transactions" t on l.from_entity_id=t.entity_iri
	left join stg_entities ef on l.from_entity_id=ef.entity_id
	left join stg_entities et on l.to_entity_id=et.entity_id
where et.type is not null;



