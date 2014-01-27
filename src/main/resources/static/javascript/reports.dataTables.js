function ReportsDataTables(reports) {
    this.reports = reports;
}

ReportsDataTables.prototype.build = function(data, id) {
    log.debug('Building dataTable for id ' + id);
    log.trace(data);

    var dataTableWrapper = $('<div class="dataTableWrapper" id="dataTableWrapper-' + id + '"><h3>' + data['name'] + '</h3></div>');
    $('#dataWrapper').append(dataTableWrapper);

    var dataTable = $('<table cellpadding="0" cellspacing="0" border="0" class="display" id="dataTable-' + id + '"></table>');
    dataTableWrapper.append(dataTable);

    var aaData = [];
    for (var i in data['values']) {
        aaData.push([data.values[i]['x'], data.values[i]['y']])
    }

    dataTable.dataTable({
        "aaData": aaData,
        "aoColumns": [
            { "sTitle": "Date" },
            { "sTitle": "Value" },
        ]
    });
}

ReportsDataTables.prototype.buildAll = function(dataForAllReports) {
    var linksWrapper = $('<div><h3>Download links</h3></div>');
    var links = $('<ul></ul>');
    linksWrapper.append(links);
    $('#dataWrapper').append(linksWrapper);

    for (var idx in dataForAllReports) {
        var hasAtLeastOneTable = false;
        for (var jdx in dataForAllReports[idx].data) {
            if (!dataForAllReports[idx].data[jdx].values || dataForAllReports[idx].data[jdx].values.length == 0) {
                continue;
            }
            this.build(dataForAllReports[idx].data[jdx], parseInt(idx) + parseInt(jdx));
            hasAtLeastOneTable = true;
        }

        if (hasAtLeastOneTable) {
            // Reports position starts at 1
            var position = parseInt(idx) + 1;
            var csvURL = this.reports.buildDataURL(position, 'csv');
            links.append('<li>' + this.reports.reports[position] + ' <a href="' + csvURL + '">csv</a></li>');
        }
    }
}