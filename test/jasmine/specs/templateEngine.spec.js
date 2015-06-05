/**
 * Created by Jorge Yagüe París.
 *
 * This tests will only work if Wesby is running.
 */

// global test namespace
var WESBY = WESBY || {};
WESBY.routes = {
    uri: "webindex/v2013/region/Europe",
    templates: "http://localhost:9000/assets/templates/",
    templatesMap: "http://localhost:9000/assets/templates/" + "template-mapping.json",
    jsonService: "http://localhost:9000/data/webindex/v2013/region/Europe"
}

describe("TemplateEngine", function() {
    var templateEngine = new TemplateEngine();
    var originalTimeout;

    /*beforeEach(function() {
        originalTimeout = jasmine.DEFAULT_TIMEOUT_INTERVAL;
        jasmine.DEFAULT_TIMEOUT_INTERVAL = 10000;
    });

    it("should load the JSON data", function(done) {
        templateEngine.loadData(function(s) {
            console.log(s);
            if(s == "success") {
                done();
            }
        });
    });*/

    it("should load the partials", function() {
        templateEngine.loadPartials();

        //expect(partials).not.toBeNull();
    });

    it("should load the template names", function() {
        templateEngine.loadTemplateNames();

        //expect(templateName).toBe("Europe");
    });

    it("should load the template", function() {
        templateEngine.loadTemplate();

        //expect(template).not.toBeNull();
    });

    it("should render the page", function() {
        templateEngine.render();

        //expect(data).not.toBeNull();
    });
});
