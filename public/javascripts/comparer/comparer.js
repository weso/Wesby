var Comparer = new (function () {
	var result = {};

	/* Element callback */

	function countryCallback(element, selectedItems) {
		countryAllSelector.clear();
		var elements = selectedItems.getArray();
		result.country = elements.length > 0 ? "COUNTRY(" + elements + ")" : "";
		
		var help = "You have selected <strong>" + element.label;
		help += "</strong>. There are <strong>" + elements.length + 
		"</strong> selected countries. You can click on each one to unselect.";
		showHelp(help);
	}
	
	function indicatorCallback(element, selectedItems) {
		indicatorAllSelector.clear();
		var elements = selectedItems.getArray();
		result.indicator = elements.length > 0 ? "INDICATOR(" + elements + ")" : "";
		
		var help = "You have selected <strong>" + element.label;
		help += "</strong>. There are <strong>" + elements.length + 
		"</strong> selected indicators. You can click on each one to unselect.";
		showHelp(help);
	}
	
	function yearCallback(element, selectedItems) {
		yearAllSelector.clear();
		var elements = selectedItems.getArray();
		result.year = elements.length > 0 ? "YEAR(" + elements + ")" : "";
			
		var help = "You have selected <strong>" + element.label;
		help += "</strong>. There are <strong>" + elements.length + 
		"</strong> selected years. You can click on each one to unselect.";
		showHelp(help);
	}
	
	/* ALL callback */
	
	function countryAllCallback(element, selectedItems) {
		countrySelector.clear();
		result.country = "COUNTRY(ALL)";
		var help = "You have selected <strong>All countries</strong>";
		showHelp(help);
	}
	
	function indicatorAllCallback(element, selectedItems) {
		indicatorSelector.clear();
		result.indicator = "INDICATOR(ALL)";
		var help = "You have selected <strong>All indicators</strong>";
		showHelp(help);
	}
	
	function yearAllCallback(element, selectedItems) {
		yearSelector.clear();
		result.year = "YEAR(ALL)";
		var help = "You have selected <strong>All years</strong>";
		showHelp(help);
	}
	
	function showHelp(text) {
		var help = document.getElementById("help")
		help.innerHTML = text;
		help.style.display = "block";
	}
	/*
	dataCountries.unshift({
		label: "ALL",
		code: "ALL"
	});
	
	dataIndicators.unshift({
		label: "ALL",
		code: "ALL"
	});
	
	dataYears.unshift({
		label: "ALL",
		code: "ALL"
	});
	*/
	
	var all = [{
		label: "ALL",
		code: "ALL"
	}];
	
	var countryAllSelector = new Selector(all, { callback: countryAllCallback });
	var countrySelector = new Selector(dataCountries, { callback: countryCallback, selectedItems: selectedCountries, maxSelectedItems: maxCountries });
	var countryDiv = document.getElementById("countrySelector")
	countryDiv.appendChild(countryAllSelector.render());
	countryDiv.appendChild(countrySelector.render());
	
	if (selectedCountries.length == 0)
		countryAllSelector.selectAll();
	
	var indicatorAllSelector = new Selector(all, { callback: indicatorAllCallback });
	var indicatorSelector = new Selector(dataIndicators, { callback: indicatorCallback, selectedItems: selectedIndicators, maxSelectedItems: maxIndicators });
	var indicatorDiv = document.getElementById("indicatorSelector");
	indicatorDiv.appendChild(indicatorAllSelector.render());
	indicatorDiv.appendChild(indicatorSelector.render());
	
	if (selectedIndicators.length == 0)
		indicatorAllSelector.selectAll();
	
	var yearAllSelector = new Selector(all, { callback: yearAllCallback });
	var yearSelector = new Selector(dataYears, { callback: yearCallback, maxSelectedItems: maxYears });
	var yearDiv = document.getElementById("yearSelector")
	yearDiv.appendChild(yearAllSelector.render());
	yearDiv.appendChild(yearSelector.render());
	
	yearAllSelector.selectAll();
	
	function clear() {
		countrySelector.clear();
		indicatorSelector.clear();
		yearSelector.clear();
		
		countryAllSelector.selectAll();
		indicatorAllSelector.selectAll();
		yearAllSelector.selectAll();
		
		result = {};
		
		showHelp("Selection is empty");
	}
	
	function compare() {
		var url = document.URL.split('?')[0] + "/";
	
		url += result.country + "/";
		url += result.year + "/";
		url += result.indicator;
		
		this.parentNode.action = url;
	}
	
	function setToggleColumn(clickArea, showArea) {
		document.getElementById(clickArea).onclick = function() {
			var selector = document.getElementById(showArea);
			
			if (selector.className == "selector")
				selector.className = "hidden-selector";
			else
				selector.className = "selector";
		};
	}
	
	function init() {
		setToggleColumn("countryColumn", "countrySelector");
		setToggleColumn("indicatorColumn", "indicatorSelector");
		setToggleColumn("yearColumn", "yearSelector");
		
		document.getElementById("clearButton").onclick = clear;
		document.getElementById("compareButton").onclick = compare;
	}
	
	init();
})();