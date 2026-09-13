# Payment API

API de estudo em Java 21, Spring Boot e PostgreSQL. O processamento é uma simulação local:
aprova pagamentos pendentes sem cobrar cartões, emitir PIX ou chamar provedores externos.

## Executar

1. Crie o banco PostgreSQL `payment_api`.
2. Configure a conexão em `src/main/resources/application.properties`.
   Não publique senhas; para compartilhar o projeto, use `spring.datasource.password=${DB_PASSWORD}`
   e configure a variável de ambiente na execução.
3. Execute `PaymentApiApplication` no IntelliJ, ou `./mvnw spring-boot:run` (Windows: `.\mvnw.cmd spring-boot:run`).
4. A porta local configurada é 8081.

## Endpoints

| Método | Caminho | Resultado |
|---|---|---|
| POST | /customers | Cadastra cliente |
| GET | /customers | Lista clientes |
| GET | /customers/{id} | Consulta cliente |
| PUT | /customers/{id} | Atualiza cliente |
| DELETE | /customers/{id} | Exclui cliente |
| POST | /payments | Cria pagamento PENDING |
| GET | /payments | Lista pagamentos |
| GET | /payments/{id} | Consulta pagamento |
| PATCH | /payments/{id}/cancel | PENDING → CANCELLED |
| POST | /payments/{id}/process | PENDING → APPROVED e registra SUCCESS |
| GET | /payments/{id}/transactions | Histórico ordenado, do mais antigo ao mais recente |

Pagamento de exemplo (substitua customerId por um cliente existente):

```json
{"customerId":2,"amount":150.00,"currency":"BRL","paymentMethod":"PIX"}
```

Processamento e cancelamento não recebem corpo. Pagamento inexistente retorna 404.
Processar ou cancelar um pagamento que deixou de ser PENDING retorna 409.
O histórico de um pagamento sem processamento retorna 200 com `[]`.

## Como o processamento funciona

PaymentController recebe a requisição e chama PaymentService.process.
O repositório busca o pagamento com PESSIMISTIC_WRITE dentro de uma transação:
requisições concorrentes de processamento/cancelamento aguardam sua vez.
Após verificar PENDING, o serviço gera um UUID, cria PaymentTransaction com SUCCESS
e altera Payment para APPROVED. As duas gravações são confirmadas juntas.
Uma falha que provoque rollback desfaz ambas.

PaymentTransactionService consulta o histórico e o converte para
PaymentTransactionResponse. Esse DTO retorna paymentId sem repetir o cliente e o pagamento completos.

## Testes

Execute `./mvnw test` (Windows: `.\mvnw.cmd test`), com Java 21 disponível.
Os testes usam o perfil test e H2 em memória, sem alterar o PostgreSQL local.
Cobrem aprovação e histórico, conflitos de estado, 404, validação, rollback,
dois processamentos simultâneos e processamento concorrente com cancelamento.
H2 não substitui uma validação de concorrência no PostgreSQL para produção.

## Limites desta versão

Não há autenticação, provedor real, estorno nem simulação de recusas.
PROCESSING, DECLINED, REFUNDED e transações FAILED ficam reservados para evolução.
Pagamentos e clientes ainda usam entidades nas respostas; o histórico já usa DTO.
Não exclua clientes vinculados a pagamentos: a chave estrangeira preserva essa relação;
o tratamento amigável desse conflito ainda pode ser adicionado.
