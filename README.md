# F1 Multi-Agent Strategy System

## Requirements
За стартиране е необходимо JDK 17.

## Build
Проектът използва Maven Wrapper за пакетиране.

Команда:
.\mvnw.cmd package

След успешно изпълнение пакетираният проект се намира в директорията target.

## Run
Основният клас за стартиране на системата е:

main.Launcher

## Ontology
При първото стартиране файлът:
src/main/resources/f1_ontology.owl

се копира в:
./data/f1_ontology.owl


## Reproducibility
За възпроизводимост на симулациите се използва seed, управляван чрез SimulationRandom.

## Database
За съхраняване на резултатите системата използва SQLite база данни:
f1_strategy.db