#!/usr/bin/env bash

set -Eeuxo pipefail

mkdir microservices
cd microservices

spring init \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-service \
--package-name=de.shinythings.microservices.core.product \
--groupId=de.shinythings.microservices.core.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
--language=kotlin \
product-service

spring init \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=review-service \
--package-name=de.shinythings.microservices.core.review \
--groupId=de.shinythings.microservices.core.review \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
--language=kotlin \
review-service

spring init \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=recommendation-service \
--package-name=de.shinythings.microservices.core.recommendation \
--groupId=de.shinythings.microservices.core.recommendation \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
--language=kotlin \
recommendation-service

spring init \
--build=gradle \
--java-version=11 \
--packaging=jar \
--name=product-composite-service \
--package-name=de.shinythings.microservices.composite.product \
--groupId=de.shinythings.microservices.composite.product \
--dependencies=actuator,webflux \
--version=1.0.0-SNAPSHOT \
--language=kotlin \
product-composite-service

cd ..

cd microservices/product-composite-service; ./gradlew build; cd -; \
cd microservices/product-service;           ./gradlew build; cd -; \
cd microservices/recommendation-service;    ./gradlew build; cd -; \
cd microservices/review-service;            ./gradlew build; cd -;

cat <<EOF > settings.gradle
include ':microservices:product-service'
include ':microservices:review-service'
include ':microservices:recommendation-service'
include ':microservices:product-composite-service'
EOF

cp -r microservices/product-service/gradle .
cp -r microservices/product-service/gradlew .
cp -r microservices/product-service/gradlew.bat .
cp -r microservices/product-service/.gitignore .

find microservices -depth -name "gradle" -exec rm -rfv "{}" \;
find microservices -depth -name "gradlew*" -exec rm -fv "{}" \;

./gradlew build
