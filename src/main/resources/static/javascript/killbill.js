/*
 *  'killbillGraph' is the namespace required to access all the public objects
 *
 *  PIE
 *
 *  Input for pie chart should be of the form
 *  dataforPie = {"name":"pie1", "data": [{"label":"one", "value":10}, ]};
 *
 *  LAYERS and LINES
 *
 *  Input for layers and line graphs are expected to be of the form:
 *  dataForGraph = [ {"name":"line1", "values":[{"x":"2013-01-01", "y":6}, {"x":"2013-01-02", "y":6}] },
 *                    {"name":"line2", "values":[{"x":"2013-01-01", "y":12}, {"x":"2013-01-02", "y":3}] } ];
 *
 *   There can be up to 20 lines -- limited by the color palette -- per graph; the graph can be either:
 *   - layered graph (KBLayersGraph)
 *   - lines graph (KBLinesGraph)
 *
 *   Description of the fields:
 *   - name is the 'name of the line-- as shown in the label
 *   - values are the {x,y} coordinates for each point; the x coordinates should be dates and should all be the same for each entries.
 *
 */
(function (killbillGraph, $, undefined) {


    /**
     * Input parameters to draw all the graphs
     */
    killbillGraph.KBInputGraphs = function (canvasWidth, canvasHeigth, topMargin, rightMargin, bottomMargin, leftMargin, betweenGraphMargin, graphData) {

        // We add some extra value here to make sure we have space to display the legend on the right and also to ensure that latest point in line/layers graph
        // can be displayed; obviously this is a hack, and if user wants to control that exactly, he can set it to 0 and specify the exact rightMargin required.
        this.rightMarginOffset = 300;

        this.topMargin = topMargin;
        this.rightMargin = rightMargin + this.rightMarginOffset;
        this.bottomMargin = bottomMargin;
        this.leftMargin = leftMargin;


        this.betweenGraphMargin = betweenGraphMargin;

        this.canvasWidth = canvasWidth;
        this.canvasHeigth = canvasHeigth;

        this.data = graphData;

    }


    /**
     * KBHistogram : Histogram chart
     */
    killbillGraph.KBHistogram = function (graphCanvas, title, data, width, heigth, palette) {

        // For non 'bin' histogram interesting blod post : http://www.recursion.org/d3-for-mere-mortals/

        this.graphCanvas = graphCanvas;
        this.name = name
        this.data = data;
        this.width = width;
        this.heigth = heigth;
        this.palette = palette;
        this.title = title;

        this.minValue;
        this.maxValue;


        this.computeMinMax = function () {
            var min;
            var max;
            for (var i = 0; i < this.data.length; i++) {
                if (min == null || this.data[i] < min) {
                    min = this.data[i];
                }
                if (max == null || this.data[i] > max) {
                    max = this.data[i];
                }
            }
            this.minValue = min - 1;
            this.maxValue = max + 2;
        }


        this.draw = function () {

            this.computeMinMax();

            var formatCount = d3.format(",.0f");

            var x = d3.scale.linear()
                .domain([this.minValue, this.maxValue])
                .range([0, this.width]);

            var data = d3.layout.histogram()
                .bins(x.ticks(10))
                (this.data);

            var y = d3.scale.linear()
                .domain([0, d3.max(data, function (d) {
                    return d.y;
                })])
                .range([this.heigth, 0]);

            var xAxis = d3.svg.axis()
                .scale(x)
                .orient("bottom");

            var svg = this.graphCanvas
                .append("svg")
                .attr("width", this.width)
                .attr("height", this.height)
                .append("g")
                .attr("transform", "translate(" + 0 + "," + 0 + ")");

            var bar = svg.selectAll(".bar")
                .data(data)
                .enter().append("g")
                .attr("class", "bar")
                .attr("transform", function (d) {
                    return "translate(" + x(d.x) + "," + y(d.y) + ")";
                });

            var myself = this;
            bar.append("rect")
                .attr("x", 1)
                .attr("width", x(data[0].dx) - 1)
                .attr("height", function (d) {
                    return myself.heigth - y(d.y);
                });

            bar.append("text")
                .attr("dy", ".75em")
                .attr("y", 6)
                .attr("x", x(data[0].dx) / 2)
                .attr("text-anchor", "middle")
                .text(function (d) {
                    return formatCount(d.y);
                });

            svg.append("g")
                .attr("class", "x axis")
                .attr("transform", "translate(" + 0 + "," + myself.heigth + ")")
                .call(xAxis);

            this.graphCanvas.append("svg:text")
                .attr("class", "title")
                .attr("x", (this.width - this.title.length) / 2)
                .attr("y", -30)
                .text(this.title);
        }

        this.addOnMouseHandlers = function () {
            // Not implemented
        }

    }

    /**
     * KBPie : A Pie chart
     */
    killbillGraph.KBPie = function (graphCanvas, title, inputData, width, heigth, palette) {

        // If our value is less than that -- compared to total, we don't display this is too small.
        this.minDisplayRatio = 0.05;

        this.graphCanvas = graphCanvas;
        this.name = name
        this.inputData = inputData;
        this.width = width;
        this.heigth = heigth;
        this.radius = (this.width / 4);
        this.palette = palette;
        this.title = title;

        this.totalValue = function () {
            var result = 0;
            for (var i = 0; i < this.data.length; i++) {
                result = result + this.data[i].value;
            }
            return result;
        }

        this.draw = function () {
            this.addLegend();
            this.drawPie();
            this.addOnMouseHandlers();
        }

        this.drawPie = function () {

            var vis = this.graphCanvas
                .append("svg:svg")
                .data([this.data])
                .attr("width", this.width)
                .attr("height", this.heigth)
                .append("svg:g")
                .attr("transform", "translate(" + (this.width / 2) + "," + this.radius + ")");

            var arc = d3.svg.arc()
                .outerRadius(this.radius);

            var pie = d3.layout.pie()
                .value(function (d) {
                    return d.value;
                });

            var arcs = vis.selectAll("g.slice")
                .data(pie)
                .enter()
                .append("svg:g")
                .attr("class", "slice");

            var myself = this;
            arcs.append("svg:path")
                .style("fill", function (d, i) {
                    return palette(i);
                })
                .attr("id", function (d, i) {
                    return  "arc-" + myself.data[i]['id'];
                })
                .attr("d", arc);
            this.addValues(arcs, arc);
        }

        this.getDisplayValue = function (value) {
            var total = this.totalValue();
            var minDisplayRatio = this.minDisplayRatio;
            return (value / total > minDisplayRatio) ? "inline" : "none";
        }

        this.addValues = function (arcs, arc) {

            var myself = this;
            arcs.append("svg:text")
                .attr("transform", function (d) {
                    d.innerRadius = 0;
                    d.outerRadius = this.radius;
                    return "translate(" + arc.centroid(d) + ")";
                })
                .attr("id", function (d, i) {
                    return  "arc-value-" + myself.data[i]['id'];
                })
                .attr("text-anchor", "middle")
                .text(function (d, i) {
                    return myself.data[i].value;
                })
                .attr("display", function (d, i) {
                    return  myself.getDisplayValue(myself.data[i].value);
                });
        }


        this.addLegend = function () {

            this.graphCanvas.append("svg:text")
                .attr("class", "title")
                .attr("x", (this.width - this.title.length) / 2)
                .attr("y", -30)
                .text(this.title);

            var legend = this.graphCanvas.append("g")
                .attr("class", "legend")
                .attr("height", 100)
                .attr("width", 200)
                .attr('transform', 'translate(-100,0)')


            var myself = this;
            legend.selectAll('rect')
                .data(this.data)
                .enter()
                .append("rect")
                .attr("x", this.width - 65)
                .attr("y", function (d, i) {
                    return i * 20;
                })
                .attr("id", function (d, i) {
                    return "pie-legend-" + myself.data[i]['id'];
                })
                .attr("width", 11)
                .attr("height", 11)
                .attr("rx", 3)
                .attr("ry", 3)
                .style("fill", function (d, i) {
                    var color = palette(i);
                    return color;
                })

            legend.selectAll('text')
                .data(this.data)
                .enter()
                .append("text")
                .attr("x", this.width - 52)
                .attr("y", function (d, i) {
                    return i * 20 + 9;
                })
                .text(function (d, i) {
                    var text = d.label;
                    return text;
                });
        }

        this.addOnMouseHandlers = function () {
            this.addMouseLegend();
        }

        this.addMouseLegend = function () {

            var myself = this;
            $('rect').each(function (i) {


                var curLegendId = $(this).attr("id");
                if (curLegendId === undefined || curLegendId.substring(0, 11) != "pie-legend-") {
                    return;
                }

                var arcId = $(this).attr("id").replace("pie-legend", "arc");
                var arcValueId = $(this).attr("id").replace("pie-legend", "arc-value");
                var arcValue = $("#" + arcValueId);

                var otherArcs = new Array();
                var otherArcValues = new Array();
                for (var i = 0; i < myself.data.length; i++) {

                    var curArcId = "arc-" + myself.data[i]['id'];
                    var curArcValueId = "arc-value-" + myself.data[i]['id'];
                    if (curArcId != arcId) {
                        var curArc = $("#" + curArcId);
                        otherArcs.push(curArc);
                        var curArcValue = $("#" + curArcValueId);
                        otherArcValues.push(curArcValue);
                    }
                }

                var myPieLegendRect = $(this);
                $(this).hover(function () {
                    for (var i = 0; i < otherArcs.length; i++) {
                        otherArcs[i].attr("opacity", 0.1);
                        otherArcValues[i].attr("display", "none");
                    }

                    arcValue.attr("display", "inline");
                    myPieLegendRect.attr("width", 15)
                        .attr("height", 15)
                        .attr('transform', 'translate(-3,-3)');
                }, function () {
                    for (var i = 0; i < otherArcs.length; i++) {
                        otherArcs[i].attr("opacity", 1.0);
                        otherArcValues[i].attr("display", myself.getDisplayValue(parseInt(otherArcValues[i].text())));
                    }
                    arcValue.attr("display", myself.getDisplayValue(parseInt(arcValue.text())));

                    myPieLegendRect.attr("width", 11)
                        .attr("height", 11)
                        .attr('transform', 'translate(0,0)');
                });
            });
        }

        this.addDataId = function () {
            for (var i = 0; i < this.inputData.length; i++) {
                this.inputData[i]['id'] = (Math.random() + 1).toString(36).substring(7);
            }
            return this.inputData;
        }

        this.data = this.addDataId();

    }

    /**
     * KBTimeSeriesBase : Base class for both layered and non layered graphs
     */
    killbillGraph.KBTimeSeriesBase = function (graphCanvas, title, inputData, width, heigth, palette) {

        this.graphCanvas = graphCanvas;
        this.inputData = inputData;

        this.width = width;
        this.heigth = heigth;
        this.title = title;

        // the palette function out of which we create color map
        this.palette = palette;


        this.addDataId = function () {
            for (var i = 0; i < this.inputData.length; i++) {
                this.inputData[i]['id'] = (Math.random() + 1).toString(36).substring(7);
            }
            return this.inputData;
        }

        /**
         * Create the 'x' date scale
         * - dataX is is an ordered array of all the dates
         */
        this.getScaleDate = function () {

            var dataX = this.extractKeyOrValueFromDataLayer(this.data[0], 'x');
            var minDate = new Date(dataX[0]);
            var maxDate = new Date(dataX[dataX.length - 1]);
            return d3.time.scale().domain([minDate, maxDate]).range([0, width]);
        }


        this.formatDate = function (date) {

            // We want to display a UTC date, so before we extract year, month, day info, we add the time difference
            // between our timezone and UTC
            date.setHours(date.getHours() + (date.getTimezoneOffset() / 60));
            var date_part = date.getDate();
            var month_part = date.getMonth() + 1
            var year_part = date.getFullYear();

            return moment(date).format('MM[/]DD[/]YYYY')
        }

        /**
         * Create the 'Y' axis in a new svg group
         * - scaleY is the d3 scale built based on height and y point range
         */
        this.createYAxis = function (scaleY) {
            var yAxisLeft = d3.svg.axis().scale(scaleY).ticks(6).tickSize([-(this.width + 25)]).orient("left");

            this.graphCanvas.append("svg:g")
                .attr("class", "y axis")
                .attr("id", "yaxis-" + this['id'])
                .attr("transform", "translate(-25,0)")
                .call(yAxisLeft);

            /*
            $("#yaxis-" + this['id']).children().each(function (i) {
               if ($(this).attr('class') == 'domain') {
                   //$(this).attr("display", "none");
               }
                console.log("Got element " + $(this).attr('class'));
            });
            */
        }

        /**
         * Create the 'X' axis in a new svg group
         * - dataLayer : the data for the layer format
         * - xAxisGraphGroup the group where this axis will be attached to
         * - xAxisHeightTick the height of the ticks
         */
        this.createXAxis = function (xAxisGraphGroup, xAxisHeightTick) {

            var scaleX = this.getScaleDate();
            var xAxis = d3.svg.axis().scale(scaleX).tickSize(-xAxisHeightTick).tickSubdivide(true);
            xAxisGraphGroup.append("svg:g")
                .attr("class", "x axis")
                .call(xAxis);
        }


        /**
         * Add the cirles for each point in the graph line
         *
         * This is used for both stacked and non stacked lines
         */
        this.addCirclesForGraph = function (circleGroup, lineId, dataX, dataY, scaleX, scaleY, lineColor) {

            var nodes = circleGroup.selectAll("g")
                .data(dataY)
                .enter();

            nodes.append("svg:circle")
                .attr("id", function (d, i) {
                    return "circle-" + lineId + "-" + i;
                })
                .attr("cx", function (d, i) {
                    return scaleX(new Date(dataX[i]));
                })
                .attr("cy", function (d, i) {
                    return scaleY(d);
                })
                .attr("r", 3.5)
                .attr("fill", lineColor)
                .attr("value", function (d, i) {
                    return d;
                });
        }

        this.addOverlayForGraph = function (circleGroup, lineId, dataX, dataY, scaleX, scaleY) {

            var myself = this;

            var nodes = circleGroup.selectAll("g")
                .data(dataY)
                .enter()
                .append("svg:g");

            nodes.append("svg:rect")
                .attr("id", function (d, i) {
                    return "rect-" + lineId + "-" + i;
                })
                .attr("x", function (d, i) {
                    return scaleX(new Date(dataX[i]));
                })
                .attr("y", function (d, i) {
                    return scaleY(d);
                })
                .attr("width", 140)
                .attr("height", 50)
                .attr("rx", 5)
                .attr("ry", 5)
                .attr("display", "none")
                .style("fill", function (d, i) {
                    return "#222";
                })
                .attr("transform", 'translate(10,-30)');


            nodes.append("svg:text")
                .attr("id", function (d, i) {
                    return "text-" + lineId + "-" + i + "-1";
                })
                .attr("x", function (d, i) {
                    return scaleX(new Date(dataX[i]));
                })
                .attr("y", function (d, i) {
                    return scaleY(d);
                })
                .attr("fill", "#bbb")
                .attr("display", "none")
                .text(function (d, i) {
                    return "Date = " + myself.formatDate(new Date(dataX[i]));
                })
                .attr("transform", 'translate(30,-10)');

            nodes.append("svg:text")
                .attr("id", function (d, i) {
                    return "text-" + lineId + "-" + i + "-2";
                })
                .attr("x", function (d, i) {
                    return scaleX(new Date(dataX[i]));
                })
                .attr("y", function (d, i) {
                    return scaleY(d);
                })
                .attr("fill", "#bbb")
                .attr("display", "none")
                .text(function (d, i) {
                    return "Value = " + myself.numberWithCommas(d);
                })
                .attr("transform", 'translate(30, 10)');
        }


        this.numberWithCommas = function (x) {
            var parts = x.toString().split(".");
            parts[0] = parts[0].replace(/\B(?=(\d{3})+(?!\d))/g, ",");
            return parts.join(".");
        }

        /**
         * Extract the 'x' or 'y' from dataLyer format where each entry if of the form:
         * - attr is either the 'x' or 'y'
         * - dataLayer : the data for a given layer
         * E.g:
         *  "name": "crescendo",
         *  "values": [
         *     { "x": "2010-07-08", "y":  0},
         *     ....
         */
        this.extractKeyOrValueFromDataLayer = function (dataLayer, attr) {
            var result = [];
            for (var i = 0; i < dataLayer.values.length; i++) {
                result.push(dataLayer.values[i][attr])
            }
            return result;
        }


        /**
         * Add on the path the name of the line -- not used anymore as we are using external labels
         */
        this.addPathLabel = function (graph, lineId, positionPercent) {
            graph.append("svg:g")
                .append("text")
                .attr("font-size", "15")
                .append("svg:textPath")
                .attr("xlink:href", "#path-" + lineId)
                .attr("startOffset", positionPercent)
                .text(function (d) {
                    return lineId;
                });
        }


        /**
         * Create a new group for the circles-- no translations needed
         */
        this.createCircleGroup = function (lineId) {
            return this.graphCanvas.append("svg:g")
                .attr("id", "circles-" + lineId);
        }

        this.createOverlayGroup = function (lineId) {
            return this.graphCanvas.append("svg:g")
                .attr("id", "overlay-" + lineId);
        }

        /**
         * Given a colorMap, extract the k-ieme color
         *
         * The colormap are standard d3 colormap which switch to new color every 4 colors;
         * in order to maximize the difference among colors we first get colors that are far apart
         *
         */
        this.getColor = function (k) {
            var div = Math.floor(k / 4);
            var mod = k % 4;
            var value = div + 4 * mod;
            return this.colorMap[value];
        }

        /**
         *  Create the color map from the d3 palette
         */
        this.createColorMap = function () {
            var colorMap = {}
            for (var i = 0; i < 20; i++) {
                colorMap[i] = this.palette(i);
            }
            return colorMap;
        }

        this.addLegend = function () {

            this.graphCanvas.append("svg:text")
                .attr("class", "title")
                .attr("x", (this.width - this.title.length ) / 2)
                .attr("y", -30)
                .text(this.title);

            var legend = this.graphCanvas.append("g")
                .attr("class", "legend")
                .attr("height", 100)
                .attr("width", 200)
                .attr('transform', 'translate(+80,+0)')


            var myself = this;
            legend.selectAll('rect')
                .data(this.data)
                .enter()
                .append("rect")
                .attr("id", function (d, i) {
                    return "ts-legend-" + myself.data[i]['id'];
                })
                .attr("x", this.width - 65)
                .attr("y", function (d, i) {
                    return i * 20;
                })
                .attr("width", 11)
                .attr("height", 11)
                .attr("rx", 3)
                .attr("ry", 3)
                .style("fill", function (d, i) {
                    var color = myself.getColor(i);
                    return color;
                })

            legend.selectAll('text')
                .data(this.data)
                .enter()
                .append("text")
                .attr("x", this.width - 52)
                .attr("y", function (d, i) {
                    return i * 20 + 9;
                })
                .text(function (d, i) {
                    var text = d.name;
                    return text;
                });
        }

        this.addOnMouseHandlers = function () {
            this.addMouseOverCircleForValue();
            this.addMouseLegend();
        }

        /**
         * Attach handlers to all circles so as to display value
         *
         * Note that this will attach for all graphs-- not only the one attached to that objec
         */
        this.addMouseOverCircleForValue = function () {

            $('circle').each(function (i) {

                var textId1 = $(this).attr("id").replace("circle", "text") + "-1";
                var textId2 = $(this).attr("id").replace("circle", "text") + "-2";
                var rectId = $(this).attr("id").replace("circle", "rect");

                var circleText1 = $('#'.concat(textId1));
                var circleText2 = $('#'.concat(textId2));
                var circleRect = $('#'.concat(rectId));

                $(this).hover(function () {
                    circleRect.show();
                    circleText1.show();
                    circleText2.show();
                }, function () {
                    setTimeout(
                        function () {
                            circleRect.hide();
                            circleText1.hide();
                            circleText2.hide();
                        }, 100);

                });
            });
        }


        this.performActionOnMouseHoverLegend = function () {
        }

        /* Build and save colorMap */
        this.colorMap = this.createColorMap();

        this.data = this.addDataId();
        this.id = (Math.random() + 1).toString(36).substring(7);

    }

    /**
     *  KBLayersGraph : Inherits KBTimeSeriesBase abd offers specifics for layered graphs
     */
    killbillGraph.KBLayersGraph = function (graphCanvas, title, data, width, heigth, palette) {

        killbillGraph.KBTimeSeriesBase.call(this, graphCanvas, title, data, width, heigth, palette);


        /**
         * Create the area function that defines for each point in the stack graph
         * its x, y0 (offest from previous stacked graph) and y position
         */
        this.createLayerArea = function (scaleX, scaleY) {
            var area = d3.svg.area()
                .x(function (d) {
                    return scaleX(new Date(d.x));
                })
                .y0(function (d) {
                    return scaleY(d.y0);
                })
                .y1(function (d) {
                    return scaleY(d.y + d.y0);
                });
            return area;
        }

        /**
         * Create the 'y' scale for the stack graph
         *
         * Extract min/max for each x value across all layers
         *
         */
        this.getLayerScaleValue = function () {

            var tmp = [];
            for (var i = 0; i < this.data.length; i++) {
                tmp.push(this.data[i].values)
            }

            var sumValues = [];
            for (var i = 0; i < tmp[0].length; i++) {
                var max = 0;
                for (var j = 0; j < tmp.length; j++) {
                    max = max + tmp[j][i].y;
                }
                sumValues.push(max);
            }
            var minValue = 0;
            var maxValue = 0;
            for (var i = 0; i < sumValues.length; i++) {
                if (sumValues[i] < minValue) {
                    minValue = sumValues[i];
                }
                if (sumValues[i] > maxValue) {
                    maxValue = sumValues[i];
                }
            }
            if (minValue > 0) {
                minValue = 0;
            }
            return d3.scale.linear().domain([minValue, maxValue]).range([heigth, 0]);
        }


        /**
         * All all layers on the graph
         */
        this.addLayers = function (stack, area, dataLayers) {

            var dataLayerStack = stack(dataLayers);

            var currentObj = this;

            this.graphCanvas.selectAll("path")
                .data(dataLayerStack)
                .enter()
                .append("path")
                .style("fill",function (d, i) {
                    return currentObj.getColor(i);
                }).attr("d", function (d) {
                    return area(d.values);
                })
                .attr("id", function (d) {
                    return "path-" + d.id;
                });
        }

        this.draw = function () {
            this.addLegend();
            this.drawStackLayers();
            this.addOnMouseHandlers();
        }

        /**
         * Draw all layers-- calls previous function addLayers
         * It will create its Y axis
         */
        this.drawStackLayers = function () {

            var scaleX = this.getScaleDate();
            var scaleY = this.getLayerScaleValue();

            var stack = d3.layout.stack()
                .offset("zero")
                .values(function (d) {
                    return d.values;
                });

            var area = this.createLayerArea(scaleX, scaleY);

            this.addLayers(stack, area, this.data);

            var dataX = this.extractKeyOrValueFromDataLayer(this.data[0], 'x');
            var dataY0 = null;
            for (var i = 0; i < this.data.length; i++) {

                var circleGroup = this.createCircleGroup(this.data[i]['id']);
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[i], 'y');
                if (dataY0) {
                    for (var k = 0; k < dataY.length; k++) {
                        dataY[k] = dataY[k] + dataY0[k];
                    }
                }
                this.addCirclesForGraph(circleGroup, this.data[i]['id'], dataX, dataY, scaleX, scaleY, this.getColor(i));
                dataY0 = dataY;
            }

            this.createYAxis(scaleY);

            dataY0 = null;
            for (var i = 0; i < this.data.length; i++) {
                var circleGroup = this.createOverlayGroup(this.data[i]['id']);
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[i], 'y');
                if (dataY0) {
                    for (var k = 0; k < dataY.length; k++) {
                        dataY[k] = dataY[k] + dataY0[k];
                    }
                }
                this.addOverlayForGraph(circleGroup, this.data[i]['id'], dataX, dataY, scaleX, scaleY);
                dataY0 = dataY;
            }

        }

        this.addMouseLegend = function () {

            var myself = this;
            $('rect').each(function (i) {

                var curLegendId = $(this).attr("id");
                if (curLegendId === undefined || curLegendId.substring(0, 10) != "ts-legend-") {
                    return;
                }

                var pathId = $(this).attr("id").replace("ts-legend", "path");
                var path = $("#" + pathId);

                var otherPaths = new Array();
                var otherCircles = new Array();
                for (var i = 0; i < myself.data.length; i++) {
                    if ("path-" + myself.data[i]['id'] != pathId) {
                        var curPath = $("#path-" + myself.data[i]['id']);
                        otherPaths.push(curPath);

                        var curCircle = $("#circles-" + myself.data[i]['id']);
                        otherCircles.push(curCircle);
                    }
                }

                var myLegendRect = $(this);
                $(this).hover(function () {
                    for (var i = 0; i < otherPaths.length; i++) {
                        otherPaths[i].attr("opacity", 0.1);
                        otherCircles[i].attr("opacity", 0);
                    }

                    myLegendRect.attr("width", 15)
                        .attr("height", 15)
                        .attr('transform', 'translate(-3,-3)');
                }, function () {
                    setTimeout(
                        function () {

                            for (var i = 0; i < otherPaths.length; i++) {
                                otherPaths[i].attr("opacity", 1.0);
                                otherCircles[i].attr("opacity", 1.0);
                            }
                            myLegendRect.attr("width", 11)
                                .attr("height", 11)
                                .attr('transform', 'translate(0,0)');
                        }, 100);
                });
            });
        }
    }
    killbillGraph.KBLayersGraph.prototype = Object.create(killbillGraph.KBTimeSeriesBase.prototype);


    /**
     *  KBLinesGraph : Inherits KBTimeSeriesBase abd offers specifics for layered graphs
     */
    killbillGraph.KBLinesGraph = function (graphCanvas, title, data, width, heigth, palette) {

        killbillGraph.KBTimeSeriesBase.call(this, graphCanvas, title, data, width, heigth, palette);

        /**
         * Create the 'y' scale for line graphs (non stacked)
         */
        this.getScaleValue = function () {

            var dataYs = [];
            for (var k = 0; k < this.data.length; k++) {
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[k], 'y');
                dataYs.push(dataY);
            }

            var minValue = 0;
            var maxValue = 0;
            for (var i = 0; i < dataYs.length; i++) {
                for (var j = 0; j < dataYs[i].length; j++) {
                    if (dataYs[i][j] < minValue) {
                        minValue = dataYs[i][j];
                    }
                    if (dataYs[i][j] > maxValue) {
                        maxValue = dataYs[i][j];
                    }
                }
            }
            if (minValue > 0) {
                minValue = 0;
            }
            return d3.scale.linear().domain([minValue, maxValue]).range([this.heigth, 0]);
        }

        /**
         * Add the svg line for this data (dataX, dataY)
         */
        this.addLine = function (dataY, scaleX, scaleY, lineColor, lineId) {

            var dataX = this.extractKeyOrValueFromDataLayer(this.data[0], 'x');
            this.graphCanvas.selectAll("path.line")
                .data([dataY])
                .enter()
                .append("svg:path")
                .attr("stroke-width", 1.5)
                .attr("d", d3.svg.line()
                    .x(function (d, i) {
                        return scaleX(new Date(dataX[i]));
                    })
                    .y(function (d) {
                        return scaleY(d);
                    }))
                .attr("id", "path-" + lineId)
                .style("stroke", lineColor);
        }

        this.draw = function () {
            this.addLegend();
            this.drawLines();
            this.addOnMouseHandlers();
        }

        /**
         * Draw all lines
         * It will create its Y axis
         */
        this.drawLines = function () {

            var scaleX = this.getScaleDate();
            var scaleY = this.getScaleValue();

            for (var k = 0; k < this.data.length; k++) {
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[k], 'y');
                this.addLine(dataY, scaleX, scaleY, this.getColor(k), this.data[k]['id']);
            }

            for (var k = 0; k < this.data.length; k++) {
                var dataX = this.extractKeyOrValueFromDataLayer(this.data[0], 'x');
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[k], 'y');
                var lineId = this.data[k]['id']
                var circleGroup = this.createCircleGroup(lineId);
                this.addCirclesForGraph(circleGroup, lineId, dataX, dataY, scaleX, scaleY, this.getColor(k));
            }

            this.createYAxis(scaleY);

            for (var k = 0; k < this.data.length; k++) {
                var dataX = this.extractKeyOrValueFromDataLayer(this.data[0], 'x');
                var dataY = this.extractKeyOrValueFromDataLayer(this.data[k], 'y');
                var lineId = this.data[k]['id']
                var circleGroup = this.createOverlayGroup(lineId);
                this.addOverlayForGraph(circleGroup, lineId, dataX, dataY, scaleX, scaleY);

            }
        }

        this.addMouseLegend = function () {

            $('rect').each(function (i) {

                var curLegendId = $(this).attr("id");
                if (curLegendId === undefined || curLegendId.substring(0, 10) != "ts-legend-") {
                    return;
                }

                var pathId = $(this).attr("id").replace("ts-legend", "path");
                var path = $("#" + pathId);

                var myLegendRect = $(this);
                $(this).hover(function () {
                    path.attr("stroke-width", 3);
                    myLegendRect.attr("width", 15)
                        .attr("height", 15)
                        .attr('transform', 'translate(-3,-3)');
                }, function () {
                    setTimeout(
                        function () {
                            path.attr("stroke-width", 1.5);
                            myLegendRect.attr("width", 11)
                                .attr("height", 11)
                                .attr('transform', 'translate(0,0)');
                        }, 100);
                });
            });
        }
    }

    killbillGraph.KBLinesGraph.prototype = Object.create(killbillGraph.KBTimeSeriesBase.prototype);


    killbillGraph.GraphStructure = function () {

        /**
         * Setup the main divs for both legend and main charts
         *
         * It is expected to have a mnain div anchir on the html with id = 'chartAnchor'.
         */
        this.setupDomStructure = function () {

            var $divChart = $('<div id="charts" class="charts">');
            var $spanChart = $('<span id="chartId" class="charts"></span>');
            $divChart.prepend($spanChart);

            $("#chartAnchor").append($divChart);
        }


        /**
         * Create initial canvas on which to draw all graphs
         */
        this.createCanvas = function (m, w, h) {
            return d3.select("#chartId")
                .append("svg:svg")
                .attr("width", w + m[1] + m[3])
                .attr("height", h + m[0] + m[2]);
        }


        /**
         * Create a new group and make the translation to leave room for margins
         */
        this.createCanvasGroup = function (canvas, translateX, translateY) {
            return canvas
                .append("svg:g")
                .attr("transform", "translate(" + translateX + "," + translateY + ")");

        }
    };

}(window.killbillGraph = window.killbillGraph || {}, jQuery)
    )
;
