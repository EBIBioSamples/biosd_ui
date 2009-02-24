//
//  AE Index Page Scripting Support. Requires jQuery 1.2.3 and JSDefeered.jQuery 0.2.1
//

var user = "";

function
aeMoreLess()
{
    $("div.ae_intro_more").toggle();
}

function
aeSwitchToAtlas()
{
    $("#ae_warehouse_box").hide();
    $("#ae_atlas_box").show();
    $.cookie("AeAtlasOption", "atlas", { expires: 365, path: '/' });
}

function
aeSwitchToAew()
{
    $("#ae_atlas_box").hide();
    $("#ae_warehouse_box").show();
    $.cookie("AeAtlasOption", null, {path: '/' });
}

function
aeShowLoginForm()
{
    $("#aer_login_link").hide();
    $("#aer_help_link").hide();
    $("#aer_login_status").text("");
    $("#aer_login_box").show();
    $("#aer_user_field").focus();
}

function
aeHideLoginForm()
{
    $("#aer_login_box").hide();
    $("#aer_login_status").text("");
    if ( "" != user ) {
        $("#aer_login_link").hide();
        $("#aer_login_info strong").text(user);
        $("#aer_login_info").show();
    } else {
        $("#aer_login_link").show();
    }
    $("#aer_help_link").show();
}


function
aeDoLogin()
{
    user = $("#aer_user_field").val();
    var pass = $("#aer_pass_field").val();
    $("#aer_pass_field").val("");
    $("#aer_login_status").text("");
    $("#aer_login_submit").attr("disabled", "true");
    $.get("verify-login.txt", { u: user, p: pass }).next(aeDoLoginNext);
}

function
aeDoLoginNext(text)
{
    if ( "" != text ) {
        $("#aer_login_box").hide();
        $("#aer_login_submit").removeAttr("disabled");

        var loginExpiration = null;
        if ( $("#aer_login_remember").attr("checked") ) {
            loginExpiration = 365;
        }
        
        $.cookie("AeLoggedUser", user, {expires: loginExpiration, path: '/'});
        $.cookie("AeLoginToken", text, {expires: loginExpiration, path: '/'});

        $("#aer_login_info strong").text(user);
        $("#aer_login_info").show();
        $("#aer_help_link").show();
        $("#aer_avail_info").text("Updating data, please wait...");
        $.get("ae-stats.xml").next(updateAerStats);        
    } else {
        user = "";
        $("#aer_login_status").text("Incorrect user name or password. Please try again.");
        $("#aer_login_submit").removeAttr("disabled");
        $("#aer_user_field").focus();
    }
}

function
aeDoLogout(shouldUpdateStats)
{
    $("#aer_login_info").hide();
    $("#aer_login_link").show();
    $.cookie("AeLoggedUser", null, {path: '/' });
    $.cookie("AeLoginToken", null, {path: '/' });
    user = "";
    if (undefined == shouldUpdateStats || shouldUpdateStats) {
        $("#aer_avail_info").text("Updating data, please wait...");
        $.get("ae-stats.xml").next(updateAerStats);
    }
}

function
aeOnLoad()
{
    // step 0: hack for IE to work with this funny EBI header/footer (to be redeveloped with jQuery)
    if (-1 != navigator.userAgent.indexOf('MSIE')) {
        document.getElementById('head').allowTransparency = true;
        // step 0.5: display a warning for those unlucky who use IE5.x
        if ( -1 != navigator.userAgent.indexOf('MSIE 5')) {
            document.getElementById('ae_jquery_unsupported').style.display = 'block';
        }
    }
}

// runs on page reload after rendering is done
$(document).ready(function()
{
    // footer is hidden by default to prevent its ugly appearance on IE if scripting is disabled
    $("#ebi_footer").show();

    // check if there is a old-fasioned request which
    // we need to dispatch to the new browse interface
    if ("" != window.location.hash) {
        var hash = String(window.location.hash);
        if ( -1 != hash.indexOf("ae-browse") ) {
        var location = "browse.html";
            var pattern = new RegExp("ae-browse\/q=([^\[]*)", "ig");
            var results = pattern.exec(hash);
            if (undefined != results && undefined != results[1]) {
                location = location + "?keywords=" + results[1];
            }
            window.location.href = location;
        }
    }
    var atlas = $.cookie("AeAtlasOption");
    if ( "atlas" == atlas ) {
        aeSwitchToAtlas();
    }

    var _user = $.cookie("AeLoggedUser");
    var _token = $.cookie("AeLoginToken");
    if ( undefined != _user && undefined != _token ) {
        user = _user;
        $("#aer_login_link").hide();
        $("#aer_login_info strong").text(user);
        $("#aer_login_info").show();
    }

    // adds a callback to close a login form on escape
    $("#aer_login_form input").keydown(
            function (e) {
                if (e.keyCode == 27) {
                    aeHideLoginForm();
                }
            }
        );

    // gets aer stats and updates the page
    $.get("ae-stats.xml").next(updateAerStats);

    // gets aew stats and updates the page
    Deferred.parallel([
        $.get("${interface.application.link.solr_gene_stats.url}").next(getNumDocsFromSolrStats),
        $.get("${interface.application.link.solr_exp_stats.url}").next(getNumDocsFromSolrStats)
    ]).next(function (values)
    {
        var aew_avail_info = values[1] + " experiments, " + values[0] + " genes available";
        $("#aew_avail_info").text(aew_avail_info);
        $("#atlas_avail_info").text(aew_avail_info);
    });

    // loads news page
    $("#ae_news").load("${interface.application.link.news_xml.url} div ul");

    // loads links page
    $("#ae_links").load("${interface.application.link.links_xml.url} div ul");
    $("#ae_news_links_area").show();
});

function
trimString(stringToTrim)
{
    return String(stringToTrim).replace(/^\s+|\s+$/g, "");
}

function
updateAerStats(xml)
{
    var aer_avail_info = "The information is unavailable at the moment";
    if (undefined != xml) {
        var ae_repxml = $($(xml).find("experiments")[0]);
        var etotal = ae_repxml.attr("total");
        var htotal = ae_repxml.attr("total-assays");
        if (etotal != undefined && etotal > 0) {
            aer_avail_info = etotal + " experiments, " + htotal + " assays available";
        }
    }
    $("#aer_avail_info").text(aer_avail_info);

    // verifying AeLoggedUser cookie still here
    if ("" != user && $.cookie("AeLoggedUser") != user) {
        // cookie was removed :)
        alert("The session for user " + user + " has expired.");
        aeDoLogout(false);
    }
}

function
getNumDocsFromSolrStats(xml)
{
    return trimString($($(xml).find("stat[name='numDocs']")[0]).text());
}