
# PR CheckList

Before submitting a pull request, please make sure the following is done:


- [ ] Followed the coding style of the project
- [ ] Used Lombok for boilerplate code.
- [ ] Added [JavaDoc](https://github.com/mykaarma/kmanage-api/blob/master-v2/server/src/main/java/com/mykaarma/kmanage/common/config/ManageDBConfig.java#L20-L26) for the new methods introduced.
- [ ] Used [JPA projections](https://github.com/mykaarma/kmanage-api/blob/master-v2/server/src/main/java/com/mykaarma/kmanage/v2/model/jpa/mykaarma/GetDealerProjection.java) for fetching selective data across multiple table instead of plain SQL queries.
- [ ] [Used Constructor Injection, didn't use field injection](https://docs.spring.io/spring/docs/4.3.12.RELEASE/spring-framework-reference/htmlsingle/#beans-constructor-injection)
- [ ] Defined DTO's in corresponding packages
- [ ] verified that new endpoints(if any) are visible in Swagger.
- [ ] Tested the changes in your GVM
- [ ] verified the changes from both [Client] and Swagger(Json).
