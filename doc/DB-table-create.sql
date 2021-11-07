CREATE TABLE "users"
(
    "user_id"    uuid PRIMARY KEY NOT NULL,
    "email"      varchar(255),
    "first_name" varchar(255),
    "last_name"  varchar(255),
    "password"   varchar(255)
);
CREATE TABLE "projects"
(
    "project_id"   uuid PRIMARY KEY NOT NULL,

    "project_name" varchar(255),
    "private"      boolean,
    "creation"     date,
    "owner"        uuid,
    "description"  varchar(255)
);
CREATE TABLE "files"
(
    "file_id"    uuid PRIMARY KEY NOT NULL,
    "file_name"  varchar(255),
    "sub_folder" varchar(255),
    "in_project" uuid
);
CREATE TABLE "tags"
(
    "tag_name" varchar(255) PRIMARY KEY
);
CREATE TABLE "roles"
(
    "role_name" varchar(255) PRIMARY KEY
);

CREATE TABLE "project_tags"
(
    "project_id" uuid,
    "tag_name"   varchar(255)
);
CREATE TABLE "file_tags"
(
    "file_id"  uuid,
    "tag_name" varchar(255)
);
CREATE TABLE "user_roles"
(
    "user_id"   uuid,
    "role_name" varchar(255)
);
CREATE TABLE "project_members"
(
    "user_id"    uuid,
    "project_id" uuid
);

CREATE TABLE "project_special_permission"
(
    "user_id"    uuid,
    "project_id" uuid
);

ALTER TABLE "projects"
    ADD FOREIGN KEY ("owner") REFERENCES "users" ("user_id");

ALTER TABLE "files"
    ADD FOREIGN KEY ("in_project") REFERENCES "projects" ("project_id");

ALTER TABLE "project_tags"
    ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("project_id");

ALTER TABLE "project_tags"
    ADD FOREIGN KEY ("tag_name") REFERENCES "tags" ("tag_name");

ALTER TABLE "file_tags"
    ADD FOREIGN KEY ("file_id") REFERENCES "files" ("file_id");

ALTER TABLE "file_tags"
    ADD FOREIGN KEY ("tag_name") REFERENCES "tags" ("tag_name");

ALTER TABLE "user_roles"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "user_roles"
    ADD FOREIGN KEY ("role_name") REFERENCES "roles" ("role_name");

ALTER TABLE "project_members"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "project_members"
    ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("project_id");

ALTER TABLE "project_special_permission"
    ADD FOREIGN KEY ("user_id") REFERENCES "users" ("user_id");

ALTER TABLE "project_special_permission"
    ADD FOREIGN KEY ("project_id") REFERENCES "projects" ("project_id");

INSERT INTO "roles" (role_name) VALUES ('ROLE_ADMIN');
INSERT INTO "roles" (role_name) VALUES ('ROLE_ACADEMIC');
INSERT INTO "roles" (role_name) VALUES ('ROLE_USER');
INSERT INTO "users" (user_id, email, first_name, last_name, password) VALUES ('e94fcf40-7147-4b03-a23e-276aee8257b9', 'admin@example.com', 'Admin', 'Adminson',
        '$2a$10$OrdCqh73IsnxM8/pvjwCDuuyQxnL9Jm5jv2YONOGB/pIL4vSaRa/O');
INSERT INTO user_roles (user_id, role_name)
   SELECT 'e94fcf40-7147-4b03-a23e-276aee8257b9', r.role_name FROM roles r WHERE r.role_name = 'ROLE_ADMIN';