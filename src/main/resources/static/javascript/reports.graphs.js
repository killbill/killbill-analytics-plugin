function ReportsGraphs(reports) {
    // To build the data tables
    this.reportsDataTables = new ReportsDataTables(reports);
}

ReportsGraphs.prototype.getTitle = function(defaultTitle, position) {
    var overridenTitle = $.url().param('title' + position);
    if (!overridenTitle) {
        return defaultTitle;
    } else {
        return overridenTitle;
    }
}

ReportsGraphs.prototype.getMappingType = function(inputType, position) {
    if (inputType == 'TIMELINE') {
        var layersOrLines = $.url().param('__layersOrLines' + position);
        if (!layersOrLines || layersOrLines != 'layers') {
            return 'lines';
        } else {
            return 'layers';
        }
    } else if (inputType == 'COUNTERS') {
        return 'pie';
    } else {
        return 'unknown';
    }
}

ReportsGraphs.prototype.doDrawAll = function(input) {
    var inputData = input.data;

    var nbGraphs = inputData.length + 1;
    var canvasHeigthWithMargins = input.topMargin + (nbGraphs * input.canvasHeigth) + ((nbGraphs - 1) * input.betweenGraphMargin) + input.bottomMargin;
    var canvasHeigthGraph = input.canvasHeigth;

    var translateX = input.leftMargin;

    var graphStructure = new killbillGraph.GraphStructure();


    graphStructure.setupDomStructure();
    var canvas = graphStructure.createCanvas([input.topMargin, input.rightMargin, input.bottomMargin, input.leftMargin],
            input.canvasWidth, canvasHeigthWithMargins);


    var curTranslateY = input.topMargin;
    var curTranslateLabelY = curTranslateY + (canvasHeigthGraph / 2);

    for (var i = 0; i < inputData.length; i++) {
        var curInput = inputData[i];
        var curType = this.getMappingType(curInput.type, i + 1);
        var curData = curInput.data;
        var curTitle = this.getTitle(curInput.title, i + 1);

        log.debug("Drawing '" + curTitle + "'");
        log.trace(curData);

        // Add the xAxis tick first for ordering purpose.
        var nextTranslateY = curTranslateY + canvasHeigthGraph;
        var xAxisCanvaGroup;
        if (curType == 'lines' || curType == 'layers') {
            xAxisCanvaGroup = graphStructure.createCanvasGroup(canvas, translateX, nextTranslateY);
        }

        var canvasGrp = graphStructure.createCanvasGroup(canvas, translateX, curTranslateY);
        var theGraph;
        if (curType == 'lines') {
            theGraph = new killbillGraph.KBLinesGraph(canvasGrp, curTitle, curData, input.canvasWidth, canvasHeigthGraph, d3.scale.category20b());
        } else if (curType == 'layers') {
            theGraph = new killbillGraph.KBLayersGraph(canvasGrp, curTitle, curData, input.canvasWidth, canvasHeigthGraph, d3.scale.category20c());
        } else if (curType == 'pie') {
            theGraph = new killbillGraph.KBPie(canvasGrp, curTitle, curData, input.canvasWidth, canvasHeigthGraph, d3.scale.category20c(), true);
        } else if (curType == 'histogram') {
            var canvasGrp = graphStructure.createCanvasGroup(canvas, translateX, curTranslateY);
            theGraph = new killbillGraph.KBHistogram(canvasGrp, curTitle, curData, input.canvasWidth, canvasHeigthGraph, d3.scale.category20c());
        }
        theGraph.draw();

        curTranslateLabelY = curTranslateLabelY;
        curTranslateY = nextTranslateY;

        if (curType == 'lines' || curType == 'layers') {
            theGraph.createXAxis(xAxisCanvaGroup, canvasHeigthGraph);
        }

        // Add subtitle
        this.addSubtitle(input, curData, i, curTranslateY + 20, curType);

        curTranslateY = curTranslateY + input.betweenGraphMargin;
    }
}

ReportsGraphs.prototype.addSubtitle = function(input, data, i, yOffset, curType) {
    if (curType != 'lines' && curType != 'layers') {
        return;
    }

    var subtitle = $('<div/>').attr('id', 'subtitle-' + i)
                              .css('position', 'absolute')
                              .css('top', yOffset + 'px');

    // Reports position starts at 1
    var position = i + 1;

    // Add CSV link
    var csvURL = this.reportsDataTables.buildCSVURL(position);
    var csvLink = $('<a/>').attr('href', csvURL).text('Download data (CSV)');
    subtitle.append(csvLink);

    subtitle.append("&nbsp;/&nbsp;");

    // Add DataTables link
    var modalBody = $('<div/>').attr('class', 'modal-body');
    for (var jdx in data) {
        if (!data[jdx].values || data[jdx].values.length == 0) {
            continue;
        }
        this.reportsDataTables.build(data[jdx], i + parseInt(jdx), modalBody);
    }

    var modal = $('<div/>').attr('class', 'modal fade')
                           .attr('id', 'dataTablesModalWrapper-' + i)
                           .attr('tabindex', '-1')
                           .attr('role', 'dialog')
                           .attr('aria-labelledby', 'RawData')
                           .attr('aria-hidden', 'true')
                           .append($('<div/>').attr('class', 'modal-dialog')
                                              .append($('<div/>').attr('class', 'modal-content')
                                                                 .append(modalBody)));
    subtitle.append(modal);
    $('#dataTablesModalWrapper-' + i).modal();

    var dataTablesLink = $('<a/>').text('View data').css('cursor', 'pointer');
    dataTablesLink.click(function() {
        $('#dataTablesModalWrapper-' + i).modal('toggle');
    });
    subtitle.append(dataTablesLink);

    subtitle.append("&nbsp;|&nbsp;");

    // Add style link
    var layersOrLines = $.url().param('__layersOrLines' + position);
    if (!layersOrLines || layersOrLines != 'layers') {
        var layersOrLinesLink = $('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURL() + '&__layersOrLines' + position + '=layers').text('Switch to layers');
    } else {
        var layersOrLinesLink = $('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURL() + '&__layersOrLines' + position + '=lines').text('Switch to lines');
    }
    subtitle.append(layersOrLinesLink);

    // Add smoothing links
    var firstLink = true;
    var smooth = $.url().param('smooth' + position);
    if (smooth) {
        firstLink ? subtitle.append("&nbsp;|&nbsp;") : subtitle.append("&nbsp;/&nbsp;");
        firstLink = false;
        subtitle.append($('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURLForNewSmooth(position)).text('Raw data'));
    }
    if (smooth != 'AVERAGE_WEEKLY') {
        firstLink ? subtitle.append("&nbsp;|&nbsp;") : subtitle.append("&nbsp;/&nbsp;");
        firstLink = false;
        subtitle.append($('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURLForNewSmooth(position, 'AVERAGE_WEEKLY')).text('Weekly average'));
    }
    if (smooth != 'AVERAGE_MONTHLY') {
        firstLink ? subtitle.append("&nbsp;|&nbsp;") : subtitle.append("&nbsp;/&nbsp;");
        firstLink = false;
        subtitle.append($('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURLForNewSmooth(position, 'AVERAGE_MONTHLY')).text('Monthly average'));
    }
    if (smooth != 'SUM_WEEKLY') {
        firstLink ? subtitle.append("&nbsp;|&nbsp;") : subtitle.append("&nbsp;/&nbsp;");
        firstLink = false;
        subtitle.append($('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURLForNewSmooth(position, 'SUM_WEEKLY')).text('Weekly sum'));
    }
    if (smooth != 'SUM_MONTHLY') {
        firstLink ? subtitle.append("&nbsp;|&nbsp;") : subtitle.append("&nbsp;/&nbsp;");
        firstLink = false;
        subtitle.append($('<a/>').attr('href', this.reportsDataTables.reports.buildRefreshURLForNewSmooth(position, 'SUM_MONTHLY')).text('Monthly sum'));
    }

    $('#charts').append(subtitle);
    // Need to append the element first before being able to get the width
    subtitle.css('left', (input.leftMargin + input.canvasWidth / 2 - subtitle.width() / 2) + 'px')
}

ReportsGraphs.prototype.drawAll = function(dataForAllReports) {
    var input = new killbillGraph.KBInputGraphs(800, 400, 80, 80, 80, 80, 160, dataForAllReports);
    this.doDrawAll(input);
}
