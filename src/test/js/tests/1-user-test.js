var chakram = require('chakram');
var expect = chakram.expect;
var shared = require('./shared');

describe("User assertions", function () {
  it("should not login", function () {
    return chakram.get("http://localhost:8080/zika-api/auth/", {
      headers: {
        'Authorization': 'Basic ' + Buffer.from('marcio@zika.com.br:123456').toString('base64')
      }
    }).then(function(response) {
      expect(response).to.have.status(401);
    });
  });

  it("should create a user", function () {
    return chakram.post("http://localhost:8080/zika-api/user",
      {
        name: 'Marcio Aguiar',
        username: 'marcio@zika.com.br',
        password: '123456',
      }
    ).then(function(response) {
      expect(response).to.have.status(201);
    });
  });

  it("should login", function () {
    return chakram.get("http://localhost:8080/zika-api/auth/", {
      headers: {
        'Authorization': 'Basic ' + Buffer.from('marcio@zika.com.br:123456').toString('base64')
      }
    }).then(function(response) {
      shared.marcio = response.body.hashedToken;
      console.log('set token', shared.marcio);
      expect(response).to.have.status(201);
    });
  });

  it("should find by username", function () {
    return chakram.get("http://localhost:8080/zika-api/user/find-by-username?username=marcio@zika.com.br", {
      headers: {
        'X-Auth-User-Token': shared.marcio
      }
    }).then(function(response) {
      expect(response).to.have.status(200);
      expect(response.body.name).to.contain("Marcio Aguiar");
    });
  });
  //X-Auth-User-Token
});
