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

(function($, undefined) {
	if ($ == undefined)
		throw "jQuery not loaded";

	$.fn.extend({
		aeLoginForm : function(options) {
			return this.each(function() {
				new $.AELoginForm(this, options);
			});
		}
	});

	$.AELoginForm = function(loginWindow, options) {
		var $body = $("body");
		var $window = $(loginWindow);
		var $login_form = $window.find("form").first();
		var $user = $login_form.find("input[name='u']").first();
		var $pass = $login_form.find("input[name='p']").first();
		var $open = $(options.open).first();
		var $close = $(options.close).first();
		var $status = $(options.status);
		var $status_text = $("<span class='alert'/>").appendTo($status);
		var $forgot = $(options.forgot).first();
		var $forgot_form = $window.find("form").last();
		var $email = $forgot_form.find("input[name='e']").first();
		var $accession = $forgot_form.find("input[name='a']").first();

		function verifyLoginValues() {
			if ("" == $user.val()) {
				showStatus("User name should not be empty");
				$user.focus();
				return false;
			}

			if ("" == $pass.val()) {
				showStatus("Password should not be empty");
				$pass.focus();
				return false;
			}

			hideStatus();
			return true;
		}

		function verifyForgotValues() {

			if ("" == $email.val()) {
				showStatus("User name or email should not be empty");
				$email.focus();
				return false;
			}

			if ("" == $accession.val()) {
				showStatus("Accession should not be empty");
				$accession.focus();
				return false;
			}

			if (-1 == ("=" + $accession.val() + "=").search(new RegExp(
					"=[ae]-[a-z]{4}-[0-9]+=", "i"))) {
				showStatus("Incorrect accession format (should be E-xxxx-nnnn)");
				$accession.focus();
				return false;
			}

			hideStatus();
			return true;
		}

		function isLoggedIn() {
			return (undefined != $.cookie("AeLoggedUser") && undefined != $
					.cookie("AeLoginToken"));
		}

		function clearCookies() {
			$.cookie("AeAuthMessage", null, {
				path : '/'
			});
			$.cookie("AeAuthUser", null, {
				path : '/'
			});
			$.cookie("AeLoggedUser", null, {
				path : '/'
			});
			$.cookie("AeLoginToken", null, {
				path : '/'
			});
		}

		function doLogout() {
			clearCookies();
			doReload();
		}

		function doReload() {
			window.location.href = window.location.href;
		}

		function doOpenWindow() {
			$body.bind("click", doCloseWindow);
			$window.bind("click", onWindowClick);
			hideForgotPanel();
			$window.show();
		}

		function doCloseWindow() {
			$window.unbind("click", onWindowClick);
			$body.unbind("click", doCloseWindow);
			$window.hide();
			hideStatus();
		}

		function onWindowClick(e) {
			e.stopPropagation();
		}

		function showStatus(text) {
			$status_text.text(text);
			$status.show();
		}

		function hideStatus() {
			$status.hide();
			$status_text.text();
		}

		function showForgotPanel() {
			$login_form.hide();
			$forgot_form.show();
			$forgot_form.find("input").first().focus();
		}

		function hideForgotPanel() {

			$forgot_form.hide();
			$forgot_form.find("input").first().val("");
			$login_form.show();
		}

		$login_form.submit(function() {
			return verifyLoginValues();
		});

		$forgot_form.submit(function() {
			return verifyForgotValues();
		});

		$open.click(function(e) {
			e.preventDefault();
			e.stopPropagation();
			if (isLoggedIn()) {
				doLogout();
			} else {
				doOpenWindow();
				$user.focus();
			}
		});

		$close.click(function(e) {
			e.preventDefault();
			doCloseWindow();
		});

		$forgot.find("a").click(function(e) {
			e.preventDefault();
			hideStatus();
			showForgotPanel();
		});

		$window.find("input").keydown(function(e) {
			if (27 == e.keyCode) {
				doCloseWindow();
			}
		});

		var message = $.cookie("AeAuthMessage");
		if (undefined != message) {
			var username = $.cookie("AeAuthUser");
			clearCookies();
			if (undefined != username) {
				$user.val(username);
			}
			showStatus(message.replace(/^"?(.+[^"])"?$/g, "$1"));
			doOpenWindow();
			if (undefined != username) {
				$pass.focus();
			} else {
				$user.focus();
			}
		}
	};

	$.fn.extend({

		aeFeedbackForm : function(options) {
			return this.each(function() {
				new $.AEFeedbackForm(this, options);
			});
		}
	});

	$.AEFeedbackForm = function(feedbackWindow, options) {

		var $body = $("body");
		var $window = $(feedbackWindow);
		var $form = $window.find("form").first();
		var $message = $form.find("textarea[name='m']").first();
		var $email = $form.find("input[name='e']").first();
		var $page = $form.find("input[name='p']").first();
		var $ref = $form.find("input[name='r']").first();
		var $submit = $form.find("input[type='submit']").first();
		var $open = $(options.open).first();
		var $close = $(options.close).first();

		function doOpenWindow() {
			$body.bind("click", doCloseWindow);
			$window.bind("click", onWindowClick);

			$submit.removeAttr("disabled");
			$window.show();
			$message.val("").focus();
		}

		function doCloseWindow() {
			$window.unbind("click", onWindowClick);
			$body.unbind("click", doCloseWindow);
			$window.hide();
		}

		function onWindowClick(e) {
			e.stopPropagation();
		}

		$form.submit(function() {
			$submit.attr("disabled", "true");
			$.post(contextPath + "/feedback", {
				m : $message.val(),
				e : $email.val(),
				p : $page.val(),
				r : $ref.val()
			}).always(function() {
				doCloseWindow();
			});
		});

		$open.click(function(e) {
			e.preventDefault();
			e.stopPropagation();
			doOpenWindow();
		});

		$close.click(function(e) {
			e.preventDefault();
			doCloseWindow();
		});

		$form.find("input,textarea").keydown(function(e) {
			if (27 == e.keyCode) {
				doCloseWindow();
			}
		});
	};

	function updateTableHeaders() {
		// alert("updateTableHeaders");
		$(".persist-area").each(
				function() {

					var el = $(this), offset = el.offset(), scrollTop = $(
							window).scrollTop(), floatingHeader = $(
							".floating-header", this), width = floatingHeader
							.prev().width();

					// alert(floatingHeader);
					if ((scrollTop > offset.top)
							&& (scrollTop < offset.top + el.height())) {
						floatingHeader.css({
							"visibility" : "visible",
							"width" : width
						});
					} else {
						floatingHeader.css({
							"visibility" : "hidden"
						});
					}
				});
	}

	function resizeTableHeaders() {
		// alert("resizeTableHeaders");
		$(".persist-area")
				.each(
						function() {

							var floatingHeader = $(".floating-header", this), width = floatingHeader
									.prev().width();

							if ("visible" == floatingHeader.css("visibility")) {
								floatingHeader.css({
									"visibility" : "visible",
									"width" : width
								});
							}
						});
	}

	function initPersistentHeaders() {
		var clonedHeaderRow;

		$(".persist-area").each(
				function() {
					clonedHeaderRow = $(".persist-header", this);
					clonedHeaderRow.before(clonedHeaderRow.clone()).addClass(
							"floating-header");

				});

		$(window).scroll(updateTableHeaders).resize(resizeTableHeaders)
				.trigger("scroll");
	}

	$.aeFeedback = function(e) {
		e.preventDefault();
		e.stopPropagation();
		$("li.feedback a").click();
	};

	$(function() {
		// alert("Assoc");
		
		//initializa local search-box
		var keywords="";
		keywords = $.query.get("keywords") || keywords;
		//alert("key->" + keywords);
		// initialize the keywords input with the search string from the homepage
		$("#local-searchbox").val(keywords);
		
		initPersistentHeaders();
		$("#ae-login").aeLoginForm({
			open : "li.login a",
			close : "#ae-login-close",
			status : ".ae-login-status",
			forgot : "#ae-login-forgot"
		});
		$("#ae-feedback").aeFeedbackForm({
			open : "li.feedback a",
			close : "#ae-feedback-close"
		});

//		var pageName = "";
//		if(/\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))!=null){
//			pageName =/\/?([^\/]+)$/.exec(decodeURI(window.location.pathname))[1];		
//		}

		changeSearch($('#local-search'),$('#biosamples_index option:selected').val());
		//changeAutocomplete(pageName);


		// $("#local-searchbox").autocomplete(
		// contextPath + "/keywords.txt"
		// , { matchContains: false
		// , selectFirst: false
		// , scroll: true
		// , max: 50
		// ,fields: []
		// , requestTreeUrl: contextPath + "/efotree.txt"
		// }
		// ).focus(autoCompleteFixSet).blur(autoCompleteFixUnset).removeAttr('autocomplete');

		// if ($("#noresults").length == 0) {
		// try {
		// /* The simplest implementation, used on your zero search results
		// pages */
		// alert("fdfd");
		// updateSummary({searchBaseURL: "http://www.ebi.ac.uk/ebisearch/"});
		// } catch (except_1) {}
		//
		// }

	});

})(window.jQuery);

// I will clear and sumit the form with no data (refresh) [PT:44656245]
function aeClearField(sel) {
	$(sel).val("").focus();
	document.forms['bs_query_form'].submit();
}

// install a proxy to all jquery requests (I will need to change the URL when
// I'm calling the ebisearch in the internal environments

$.ajaxSetup({
	// crossDomain: true,
	beforeSend : function(xhr, opts) {
		//alert("url:"+opts.url);
		if (opts.url.indexOf("/ebisearch/") == 0
				&& !((opts.url.indexOf("www.ebi.ac.uk") == 0) || (opts.url
						.indexOf("wwwdev.ebi.ac.uk") == 0))) {
			opts.url = "http://www.ebi.ac.uk" + opts.url;
			opts.crossDomain = true;
		}

		 //alert(document.domain);
		// show progress spinner
	},
	complete : function() {
		// hide progress spinner	
	}
});

// To control the different searches on the header (by group and by samples)
function changeSearch(form, url) {
	// alert("fdfd->" + $("#biosamples_index option:selected").val());
	// alert(form.getAttribute('action'));
	form.attr("action", url);
	//alert("changeAutocomplete changeSearch");
	changeAutocomplete(url);
}

function submitFormForExamples(form, url, extraParameters) {
	// alert($('#local-searchbox').val());
	
	//alert('form->' + form);
	//alert('form->'+form.attr("action"));
	$('#local-searchbox').val(extraParameters);
	form.submit();
}

function changeAutocomplete(url) {
	//alert("changeAutocomplete->" + url);
	var autoCompleteFixSet = function() {
		$(this).attr('autocomplete', 'off');
	};
	var autoCompleteFixUnset = function() {
		$(this).removeAttr('autocomplete');
	};
	if (url.match(/browse_samples/i)) {
		//alert("match c sample");
		$("#local-searchbox").autocomplete(
				contextPath + "/keywords.txt?domain=biosamplessample" /*
																		 * search
																		 * in
																		 * samples
																		 * domain
																		 */
				, {
					matchContains : false,
					selectFirst : false,
					scroll : true,
					max : 250 /* more fields to filter */
					,
					fields : [],
					requestTreeUrl : contextPath + "/efotree.txt"
				}).focus(autoCompleteFixSet).blur(autoCompleteFixUnset)
				.removeAttr('autocomplete');

	} else {
		$("#local-searchbox").autocomplete(contextPath + "/keywords.txt", {
			matchContains : false,
			selectFirst : false,
			scroll : true,
			max : 50,
			fields : [],
			requestTreeUrl : contextPath + "/efotree.txt"
		}).focus(autoCompleteFixSet).blur(autoCompleteFixUnset).removeAttr(
				'autocomplete');
	}
}

// To control the different searches on the header (by group and by samples)
