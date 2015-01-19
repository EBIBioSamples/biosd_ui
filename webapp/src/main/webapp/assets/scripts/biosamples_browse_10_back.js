/*
 * Copyright 2009-2011 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

var query = new Object();
var pageName = /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
var anchor = decodeURI(window.location.hash);

var sortDefault = {
	relevance : "descending",
	accession : "ascending",
	title : "ascending",
	samples : "descending",
	database : "ascending"
};

var sortTitle = {
	accession : "accession",
	title : "title",
	samples : "number of samples",
	database : "database"
};

// var sortBy = "id";
// var sortOrder = sortDefault[sortBy];

// by default I will sort by lucene algorithm
var sortBy = "relevance";
var sortOrder = sortDefault[sortBy];

var pageInit = "1";
var pageSize = "50";
var keywords = "";

$(function() {
	$("#bs_contents").show();

	// this will be executed when DOM is ready
	if ($.query == undefined)
		throw "jQuery.query not loaded";

	pageInit = $.query.get("page") || pageInit;
	pageSize = $.query.get("pagesize") || pageSize;
	keywords = $.query.get("keywords") || keywords;
	sortBy = $.query.get("sortby") || sortBy;
	// alert(sortBy);
	sortOrder = $.query.get("sortorder") || sortDefault[sortBy];
	// I need to initialize the sorting on the defaultfield
	var thElt = $("#bs_results_header_" + sortBy);
	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("span.table_header_inner");
			// alert(divElt[0]);
			// I'm using textContent because innerText doesnt work on FireFox
			if (divElt[0] != null) {
				var hasInnerText = (divElt[0].innerText != undefined) ? true
						: false;
				if (hasInnerText) {
					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-up'></i>";
					}
				} else {

					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-up'></i>";
					}
				}
			}

		}
	}

	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();

	var urlPage = "group/browse-table.html" + newQuery;
	// alert(urlPage);

	// initialize the keywords input with the search string from the homepage
	$("#bs_keywords_field").val(keywords);

	// initialize the sortby
	$("#sortby").removeAttr("disabled").val(getQueryStringParam("sortby", ""));

	QuerySampleGroup(urlPage);
	// added autocompletion
	var basePath = decodeURI(window.location.pathname).replace(/\/\w+\.\w+$/,
			"/");
	// alert(basePath);

});

// function responsible for managing the back history properly (this function
// uses the values added during the sorting + paging + pagingSize)
if (typeof (window.history.pushState) == 'function') {
	$(window).bind('popstate', function(event) {
		var state = event.originalEvent.state;
		if (state != null) {
			// alert("estado pro qual eu quero ir->" +state);
			removeAllSortingIndications();
			QuerySampleGroup(state.url);
			sortBy = state.sortby;
			sortOrder = state.sortorder;
			// alert(state.sortby + state.sortorder + "###" + state.url);
			addSortingIndication(sortBy, sortOrder);
		}
	});
}

function QuerySampleGroup(url) {
	// alert("QuerySampleGroup2->" + url);
	$
			.get(
					url,
					function(tableHtml) {
						// alert("Data->"+tableHtml);

						$("#bs_results_body_inner").removeClass(
								"bs_results_tbl_loading");

						// populate table with data
						$("#bs_results_tbody").html(tableHtml);

						// attach titles to highlight classes
						$("#bs_results_tbody")
								.find(".ae_text_hit")
								.attr("title",
										"This is exact string matched for input query terms");
						$("#bs_results_tbody")
								.find(".ae_text_syn")
								.attr("title",
										"This is synonym matched from Experimental Factor Ontology");
						$("#bs_results_tbody")
								.find(".ae_text_efo")
								.attr("title",
										"This is matched child term from Experimental Factor Ontology");

						// get stats from the first row
						var total = $("#bs_results_total").text();
						var from = $("#bs_results_from").text();
						var to = $("#bs_results_to").text();
						var curpage = $("#bs_results_page").text();
						var pagesize = $("#bs_results_pagesize").text();

						if (total > 0) {

							var totalPages = total > 0 ? Math.floor((total - 1)
									/ pagesize) + 1 : 0;
							$(".bs-stats").html(
									" Showing <span>" + from + " - " + to
											+ "</span> of <span>" + total
											+ "</span> SampleGroups");

							if (totalPages > 1) {
								var pagerHtml = "Page ";
								for ( var page = 1; page <= totalPages; page++) {
									if (curpage == page) {
										pagerHtml = pagerHtml + "<span>" + page
												+ "</span>";
									} else if (2 == page && curpage > 6
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (totalPages - 1 == page
											&& totalPages - curpage > 5
											&& totalPages > 11) {
										pagerHtml = pagerHtml + "..";
									} else if (1 == page
											|| (curpage < 7 && page < 11)
											|| (Math.abs(page - curpage) < 5)
											|| (totalPages - curpage < 6 && totalPages
													- page < 10)
											|| totalPages == page
											|| totalPages <= 11) {
										pagerHtml = pagerHtml
												+ "<a href=\"javascript:goToPage("
												+ page + ")" + ";\">" + page
												+ "</a>";
									}
								}
								$(".bs-pager").html(pagerHtml);
							} else {
								$(".bs-pager").html("&nbsp;");
							}

							// pagesize
							var arrayPageSize = [ "10", "25", "50", "100",
									"250", "500" ];
							var pageSizeHtml = "Page size ";
							for ( var i = 0; i < arrayPageSize.length; i++) {
								if (pagesize == arrayPageSize[i]) {
									pageSizeHtml += "<span>" + pagesize
											+ "</span>";
								} else {
									pageSizeHtml += "<a href=\"javascript:goToPageSize("
											+ arrayPageSize[i]
											+ ")"
											+ ";\">"
											+ arrayPageSize[i] + "</a>";
									;

								}
								// Do something with element i.
							}
							$(".bs-page-size").html(pageSizeHtml);

						} else {
							// Clean the paging information
							$(".bs-stats").html("No SampleGroups found.");
							$(".bs-page-size").html("&nbsp;");
							$(".bs-pager").html("&nbsp;");
						}
					});

}

function onQueryError() {
	// remove progress gif
	$("#bs_results_body_inner").removeClass("bs_results_tbl_loading");
	// report problem to the user
	$("#bs_results_tbody")
			.html(
					"<tr class=\"bs_results_tr_error\"><td colspan=\"9\">There was an error processing the query. Please try again later.</td></tr>");
}

function goToPage(pPage) {
	pageInit = pPage;
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var urlPage = "group/browse-table.html" + newQuery;
	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "browse.html" + newQuery;
		// I will use the urlPage to reload the data on poststate event
		window.history.pushState({
			url : urlPage,
			sortby : sortBy,
			sortorder : sortOrder
		}, "", urlState);
	}
	QuerySampleGroup(urlPage);
}

function goToPageSize(pPageSize) {
	pageSize = pPageSize;
	pageInit = 1;
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();
	var urlPage = "group/browse-table.html" + newQuery;

	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "browse.html" + newQuery;
		// I will use the urlPage to reload the data on poststate event
		window.history.pushState({
			url : urlPage,
			sortby : sortBy,
			sortorder : sortOrder
		}, "", urlState);
	}
	QuerySampleGroup(urlPage);
}

function getQueryStringParam(paramName, defaultValue) {
	var param = $.query.get(paramName);
	if ("" !== param) {
		return param;
	} else {
		return defaultValue;
	}
}

function getQueryArrayParam(paramName) {
	var param = $.query.get(paramName);
	if (!jQuery.isArray(param)) {
		return new Array(param);
	} else {
		return param;
	}
}

function getQueryBooleanParam(paramName) {
	var param = $.query.get(paramName);
	return (true === param || "" != param) ? true : undefined;
}

function aeSort(psortby) {
	// alert("sortby->" + psortby);

	// im sorting the same attribute
	if (psortby == sortBy) {
		sortOrder = (sortOrder == "ascending" ? "descending" : "ascending");
	} else {
		sortBy = psortby;
		sortOrder = sortDefault[sortBy];
	}
	// alert("sortBy->" + sortBy + ";sortOrder->" + sortOrder);
	pageInit = $.query.get("page") || pageInit;
	pageSize = $.query.get("pagesize") || pageSize;

	removeAllSortingIndications();
	var newQuery = $.query.set("keywords", keywords).set("sortby", sortBy).set(
			"sortorder", sortOrder).set("page", pageInit).set("pagesize",
			pageSize).toString();

	// var pageName =
	// /\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];
	var urlPage = "group/browse-table.html" + newQuery;

	if (typeof (window.history.pushState) == 'function') {
		// mantain the history on browser ans also change the URL for history
		// favorites purpose
		var urlState = "browse.html" + newQuery;
		// I will use the urlPage to reload the data on poststate event
		window.history.pushState({
			url : urlPage,
			sortby : sortBy,
			sortorder : sortOrder
		}, "", urlState);
	}

	QuerySampleGroup(urlPage);
	addSortingIndication(sortBy, sortOrder);

	// window.location.href = urlPage;

}

function addSortingIndication(sortBy, sortOrder) {
	// I just put the orientation after the query return the results (before I
	// clean all the asc and desc of all the columns, after I make the query and
	// at the end i Put the correct one)
	var thElt = $("#bs_results_header_" + sortBy);

	if (null != thElt) {
		// alert("#bs_results_header_" + sortBy);
		if ("" != sortOrder) {
			var divElt = thElt.find("span.table_header_inner");
			if (null != divElt) {
				var hasInnerText = ($("span.table_header_inner")[0].innerText != undefined) ? true
						: false;
				if (hasInnerText) {
					if ("descending" == sortOrder) {
						// alert(divElt[0].innerText);
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].innerText
								+ "<i class='aw-icon-angle-up'></i>";
					}
				} else {
					if ("descending" == sortOrder) {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-down'></i>";
					} else {
						divElt[0].innerHTML = divElt[0].textContent
								+ "<i class='aw-icon-angle-up'></i>";
					}
				}
			}
		}
	}
}

function removeAllSortingIndications() {
	// remove all the sort signs on the headers
	// I'm using textContent because innerText doesnt work on FireFox

	for (i = 0; i < ($("span.table_header_inner").length); i++) {
		var hasInnerText = ($("span.table_header_inner")[i].innerText != undefined) ? true
				: false;
		if (hasInnerText) {
			$("span.table_header_inner")[i].innerHTML = $("span.table_header_inner")[i].innerText; // +
			// "&nbsp;";
		} else {
			// &nbsp is used to preserve the arrow space
			$("span.table_header_inner")[i].innerHTML = $("span.table_header_inner")[i].textContent; // +
			// "&nbsp;";
		}
	}

}

function getQueryStringParam(paramName, defaultValue) {
	var param = $.query.get(paramName);
	if ("" !== param) {
		return param;
	} else {
		return defaultValue;
	}
}