var chakram = require('chakram');
var expect = chakram.expect;
var shared = require('./shared');

describe("Verificações de reportes", function () {
  it("Não deve existir reportes", function () {
    return chakram.get("http://localhost:8080/zika-api/poi", {
      headers: {
        'X-Auth-User-Token': shared.marcio
      }
    }).then(function(response) {
      console.log('noooo');
      expect(response).to.have.status(200);
      expect(response.body.page.totalElements).to.equal(0);
    });
  });

  it("Deve criar um report", function () {
    return chakram.post("http://localhost:8080/zika-api/poi", {
      location: {
        lat: -9.1,
        lng: -36.2
      },
      title: 'Foco 1',
      description: 'Pneus cheios de água',
      type: {
        id: 3
      }
    }, {
      headers: {
        'X-Auth-User-Token': shared.marcio
      }
    }).then(function(response) {
      console.log(response);
      expect(response).to.have.status(201);
    });
  });

  it("Deve conquistar Dissminador Júnior", function () {
    return true;
  });

  it("Deve conquistar Agente de Saúde Júnior", function () {
    return true;
  });
  //X-Auth-User-Token
});
