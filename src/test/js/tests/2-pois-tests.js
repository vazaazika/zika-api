var chakram = require('chakram');
var expect = chakram.expect;
var shared = require('./shared');

describe("POI assertions", function () {
  it("should find no poi", function () {
    console.log('go', shared);
    return chakram.get("http://localhost:8080/zika-api/user/poi", {
      headers: {
        'X-Auth-User-Token': shared.marcio
      }
    }).then(function(response) {
      console.log('noooo');
      expect(response).to.have.status(204);
    });
  });
  //X-Auth-User-Token
});
