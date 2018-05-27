# zika-api

Executar o script testlibs/install-testlibs.sh para que as dependências do projeto zika-core sejam adicionadas na pasta .m2

Para alterar o banco de dados que será utilizado definir o dataSource no **hibernate-config.xml**

Exemplo:
Utilizando o docker
```code
<property name="dataSource" ref="dataSourceDocker" />
```
## CORS
Ao alterar a aplicação de servidor do cliente web (mudar de uma máquina para outra) definir o hearder **Access-Control-Allow-Origin** 
na classe CORSFilter.java com o servidor onde o cliente web se enconta.

## Gerar o zika-api.war
Na raíz do projeto executar o comando
```bash
mvn clean install
```
Após isso o  **zika-api.war** pode ser encontrado em zika-api/target
