goog.require( 'goog.dom' );

createSection = function()
{
    return goog.dom.createDom( 'section' );
};

var section = createSection();
alert( msg["hello"] );