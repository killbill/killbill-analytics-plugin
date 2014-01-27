function ReportsDataTables(reports) {
    this.reports = reports;
}

ReportsDataTables.prototype.build = function(data, id, wrapper) {
    log.debug('Building dataTable for id ' + id);
    log.trace(data);

    var dataTableWrapper = $('<div class="dataTableWrapper" id="dataTableWrapper-' + id + '"><h3>' + data['name'] + '</h3></div>');
    wrapper.append(dataTableWrapper);

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

ReportsDataTables.prototype.buildCSVURL = function(position) {
    return this.reports.buildDataURL(position, 'csv');
}