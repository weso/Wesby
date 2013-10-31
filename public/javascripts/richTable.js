var richTable = new Object();

////////////////////////////////////////////////////////////////
// Pages
////////////////////////////////////////////////////////////////

richTable.pages = new (function() {
	var firstShownRow = 0;
	var numberFooterAnchors = 5; // Must be odd

	(function init() {
		var tables = document.querySelectorAll("table.pages");
		var length = tables.length;
		
		for (var i = 0; i < length; i++) {
			var table = tables[i];
			
			// Table properties
			table.isPaged = true;
			
			table.firstShownRow = firstShownRow;
			table.numberFooterAnchors = numberFooterAnchors;
		
			var tBodies = table.tBodies;
			var length = tBodies.length;
			var rowNumber = 0;
			
			for (var j = 0; j < length; j++)
				rowNumber += tBodies[j].rows.length;				

			table.numberOfRows = rowNumber;
			
			var pageLength = (table.numberOfRows == 0) ? 1 : Math.floor(log10(table.numberOfRows)) + 1;
			table.rowsPerPage = Math.pow(10, pageLength - 1);
		
			prepareTable(table);
			
			// Header select
			createHeaderSelect(table);
		}
	})();
	
	function prepareTable(table) {
		// Fitst we remove empty rows (Added to complete last page)
		var emptyRows = table.querySelectorAll("tr.empty");
		var length = emptyRows.length;
		
		for (var i = 0; i < length; i++)
			emptyRows[i].parentNode.removeChild(emptyRows[i]);

		// Table properties
		table.numberOfPages = Math.ceil(table.numberOfRows / table.rowsPerPage);

		// If last page has less rows
		var total = table.numberOfPages * table.rowsPerPage;
		var length = table.numberOfColumns > 0 ? table.numberOfColumns : 1;
		var tBodies = table.tBodies;
		
		if (table.numberOfPages > 1 && total > table.numberOfRows && tBodies.length > 0) {
			var rowNumber = table.numberOfRows;
		
			while (total > rowNumber) {
				var tr = document.createElement("tr");
				tr.className = "empty";
				
				for (var i = 0; i < length; i++) {
					var td = document.createElement("td");
					td.innerHTML = "&nbsp";
					tr.appendChild(td);
				}
				
				tBodies[0].appendChild(tr);
				
				rowNumber++;
			}
		}

		table.selectedPage = 1;
		showPage(table);
		
		table.changePage = changePage;
		table.reloadPage = function () {
			showPage(this);
		}
		
		// Footer
		createFooter(table);
	}
	
	function changePage(pageNumber) {
		if (pageNumber > this.numberOfPages || pageNumber < 1)
			return;
			
		this.firstShownRow = (pageNumber - 1) * this.rowsPerPage;
		this.selectedPage = pageNumber;
		
		showPage(this);
	}
	
	function showPage(table) {
		var firstShownRow = table.firstShownRow;
		var rowsPerPage = table.rowsPerPage * 1;
		var tBodies = table.tBodies;		
	
		// We set visible and hidden rows
		var count = 0; // All rows (including empty rows)
		var numberOfRows = 0; // Real rows
		var lastShownRow = firstShownRow + rowsPerPage - 1;
	
		var length = tBodies.length;
		var numberOfColumns = 0;
		
		for (var i = 0; i < length; i++) {
			var rows = tBodies[i].rows;
			var rowNumber = rows.length;
			
			for (var j = 0; j < rowNumber; j++) {
				var row = rows[j];
				var shown = count >= firstShownRow && count <= lastShownRow;
				row.style.display = shown == true ? "table-row" : "none";
				
				if (row.cells.length > numberOfColumns)
					numberOfColumns = row.cells.length;
				
				if (row.className != "empty")
					numberOfRows++;
				
				count++;
			}
		}
		
		// Set footer link as selected
		updateFooter(table);
		
		// Update table properties
		table.numberOfRows = numberOfRows;
		table.numberOfColumns = numberOfColumns;
	}
	
	function updateFooter(table) {
		var selectedPage = table.selectedPage;
		var numberOfPages = table.numberOfPages;
		
		// First link
		var link = table.querySelector("tFoot a.first");
		
		if (link) {
			link.className = selectedPage > 1 ? "not-number first" : "not-number first disabled";
			link.index = selectedPage > 1 ? 1 : -1;
		}
		
		// Previous link
		var link = table.querySelector("tFoot a.previous");
		
		if (link) {
			link.className = selectedPage > 1 ? "not-number previous" : "not-number previous disabled";
			link.index = selectedPage > 1 ? selectedPage - 1 : -1;		
		}
		
		// Next link
		var link = table.querySelector("tFoot a.next");
		
		if (link) {
			link.className = selectedPage < numberOfPages ? "not-number next" : "not-number next disabled";
			link.index = selectedPage < numberOfPages ? selectedPage + 1 : -1;	
		}
		
		// Last link
		var link = table.querySelector("tFoot a.last");
		
		if (link) {
			link.className = selectedPage < numberOfPages ? "not-number last" : "not-number last disabled";
			link.index = selectedPage < numberOfPages ? numberOfPages : -1;		
		}
		
		// Number links
		var links = table.querySelectorAll("tFoot a.number");
		var length = links.length;
		
		// Visible anchors
		var numberOfBubbles = Math.min(table.numberFooterAnchors, length);
		var numberFooterAnchorsHalf = Math.floor(numberOfBubbles / 2);

		var previous = selectedPage > numberFooterAnchorsHalf ? numberFooterAnchorsHalf : selectedPage - 1;
		var next = table.numberFooterAnchors - 1 - previous;
	
		var firstShown = selectedPage - previous;
		var lastShown = selectedPage + next;
		
		if (lastShown > length) {
			firstShown -= lastShown - length;
			lastShown = length;
		}
			
		for (var i = 1; i <= length; i++) {
			var shown = i >= firstShown && i <= lastShown ? "shown" : "hidden";
			var selected = i == selectedPage ? "number selected " : "number ";
			links[i - 1].className = selected + shown;
		}
		
		// Show navigation details
		var first = (selectedPage - 1) * table.rowsPerPage + 1;
		var last = first + table.rowsPerPage - 1 > table.numberOfRows ? table.numberOfRows : first + table.rowsPerPage - 1;
		var details = table.querySelector("tFoot span.details");
		
		if (details)
			details.innerHTML = first + " - " + last + " of " + table.numberOfRows + " items.";
	}
	
	function createFooter(table) {
		var footer = table.querySelector("tFoot.pagination");
	
		if (!footer) {
			var footer = document.createElement("tFoot");
			footer.className = "pagination";
			
			table.appendChild(footer);
			table.navFooter = footer;
		}
		
		footer.innerHTML = "";
		
		var tr = document.createElement("tr");
		footer.appendChild(tr);
		
		var td = document.createElement("td");
		tr.appendChild(td);
		td.colSpan = table.numberOfColumns;
		
		// Navigation details
		var span = document.createElement("span");
		span.className = "details";
		td.appendChild(span);	
		
		var first = table.numberOfRows > 0 ? 1 : 0;
		var last = table.numberOfRows > table.rowsPerPage ? table.rowsPerPage : table.numberOfRows;
		span.innerHTML = first + " - " + last + " of " + table.numberOfRows + " items.";
			
		// First link
		var a = createLink(table, "<<", "not-number first disabled", -1);
		td.appendChild(a);
		
		// Previous link
		var a = createLink(table, "<", "not-number previous disabled", -1);
		td.appendChild(a);
		
		// Number links
		var length = table.numberOfPages;
	
		for (var i = 1; i <= length; i++) {
			var shown = i > table.numberFooterAnchors ? "hidden" : "shown";
			var selected = i == 1 ? "number selected " : "number ";
			var a = createLink(table, i, selected + shown, i);
			td.appendChild(a);
		}
		
		// Next link
		var a = createLink(table, ">", length > 1 ? "not-number next" : "not-number next disabled", length > 1 ? 2 : -1);
		td.appendChild(a);		
		
		// Last link
		var a = createLink(table, ">>", length > 1 ? "not-number last" : "not-number last disabled", length > 1 ? length : -1);
		td.appendChild(a);
	}
	
	function createHeaderSelect(table) {
		var tHead = table.tHead;
		var numberOfRows = table.numberOfRows;
		var length = (numberOfRows == 0) ? 1 : Math.floor(log10(numberOfRows)) + 1;

		if (tHead && length > 1) {
			var tr = document.createElement("tr");
			tr.className = "select";
			tHead.insertBefore(tr, tHead.firstChild);
			
			var th = document.createElement("th");
			th.colSpan = table.numberOfColumns;
			tr.appendChild(th);
			
			var span = document.createElement("span");
			span.className = "show";
			th.appendChild(span);
			
			span.appendChild(document.createTextNode("Show "));
			
			var select = document.createElement("select");
			th.appendChild(select);
			
			select.table = table;
			
			var span = document.createElement("span");
			span.className = "entries";
			th.appendChild(span);
			
			span.appendChild(document.createTextNode(" entries"));
			
			//Options
			var inc = Math.pow(10, length - 1);
			
			for (var i = 1; i <= 4; i++) {
				var number = inc * i;
				
				if (number > numberOfRows)
					break;
				
				createOption(select, number, number);
			}
			
			createOption(select, "all", numberOfRows);
			
			// Handler
			select.onchange = function() {
				var value = this.options[this.selectedIndex].value;
				this.table.rowsPerPage = value;

				prepareTable(this.table);
				table.changePage(1);
			};
		}
	}
	
	function createOption(select, text, value) {
		var option = document.createElement("option");
		option.text = text;
		option.value = value;
		select.appendChild(option);
	}
	
	function log10(val) {
		return Math.log(val) / Math.LN10;
	}

	function createLink(table, text, className, index) {
		var a = document.createElement("a");
		a.className = className;
		a.table = table;
		a.index = index;
		
		a.onclick = function() {
			this.table.changePage(this.index);
		};
		
		var textNode = document.createTextNode(text);
		a.appendChild(textNode);
		
		return a;
	}
})();

////////////////////////////////////////////////////////////////
// Sortable 
////////////////////////////////////////////////////////////////

richTable.sort = new (function() {
	(function init() {
		var tables = document.querySelectorAll("table.sortable");
		var length = tables.length;
		
		for (var i = 0; i < length; i++)
			iterateOverRows(tables[i]);
	})();
	
	function iterateOverRows(table) {
		var headers = table.tHead.rows;
		//var headers = table.querySelectorAll("tHead tr");
		var length = headers.length;
	
		for (var i = 0; i < length; i++) {
			if (headers[i].className == "select")
				continue;
				
			headers[i].className = "sorter";	
			
			var rowLength = headers[i].cells.length;
		
			for (var j = 0; j < rowLength; j++) {
				headers[i].cells[j].table = table;
				headers[i].cells[j].index = j;
				headers[i].cells[j].sort = "empty";
				
				var img = new Image();
				img.className = "empty";
				headers[i].cells[j].appendChild(img);
				
				headers[i].cells[j].onclick = function() {
					sortRows(this.table, this.index, this);
				};
			}
		}
	}
	
	function sortRows(table, index, header) {
		// Insert rows in array
		
		var rows = new Array();
		
		var length = table.tBodies.length;
		
		for (var i = 0; i < length; i++) {
			var tBody = table.tBodies[i];
		
			for (var j = 0; j < tBody.rows.length; j++) {
				var row = tBody.rows[j];
			
				rows.push(row);
			}
		}
		
		// Sorting		
		if (header.sort == "empty" || header.sort == "down")
			header.sort = "up";
		else
			header.sort = "down";	
		
		rows.sort(function(row1, row2) {
			if (row1.className == "empty" || row2.className == "empty")
				return 1;

			var row1 = row1.cells[index].innerHTML.toString();
			var row2 = row2.cells[index].innerHTML.toString();	
		
			if (row1 < row2)
				return header.sort == "up" ? - 1 : 1;
			else if (row1 > row2)
				return header.sort == "up" ? 1 : -1;
			else
				return 0;
		});
		
		// Update header image
		var img = header.querySelector("img");
		img.className = header.sort;
		
		// Update header image for the rest of columns
		//var tHeader = table.tHead.rows[0].cells;
		var tHeader = table.querySelectorAll("tHead tr.sorter th");
		var length = tHeader.length;
		
		for (var i = 0; i < length; i++)
			if (i != index)
				emptyHeaderSorter(tHeader[i]);
		
		// Delete rows
		var tHead = table.tHead;
		var tFoot = table.tFoot;
		
		while(table.hasChildNodes())
			table.removeChild(table.firstChild);
			
		// Insert head
		table.appendChild(tHead);
		
		// Insert footer
		if (tFoot)
			table.appendChild(tFoot);
		
		// Insert sorted rows
		var tBody = document.createElement('tbody');
		table.appendChild(tBody);
		
		var length = rows.length;
		
		for (var i = 0; i < length; i++)
			tBody.appendChild(rows[i]);
			
		// If table is paged then page 1 is set
		if (table.changePage)	
			table.changePage(1);
	}
	
	function emptyHeaderSorter(header) {
		var img = header.querySelectorAll("img");
		
		if (img && img.length && img.length > 0)
			img[0].className = "empty";
			
		header.sort = "empty";
	}
})();