const presentationConfiguration = require('../src/presentation');

test('parses message settings and ignores malformed fields', () => {
  const message = {_st_inapp_presentation: {version: 1,
    notification: {style: 'modal', display_duration: 0, shows_close_button: false,
      background_color: {light: '#FFFFFF', dark: '#000000'}, corner_radius: 'invalid'},
    full_screen: {link_button_title: 'Read more'}}};
  expect(presentationConfiguration(message, 'notification')).toEqual({style: 'modal', display_duration: 0,
    shows_close_button: false, background_color: {light: '#FFFFFF', dark: '#000000'}});
  expect(presentationConfiguration(message, 'detail')).toEqual({link_button_title: 'Read more'});
  expect(presentationConfiguration({_st_inapp_presentation: {version: 2, notification: {}}}, 'notification')).toBeNull();
});

test('exports the accessor on MessageStream', () => {
  const {MessageStream} = require('../src/index');
  expect(MessageStream.getMessagePresentationConfiguration({_st_inapp_presentation: {notification: {shows_close_button: false}}}, 'notification')).toEqual({shows_close_button: false});
});


test('rejects out-of-range presentation settings while preserving zero', () => {
  const settings = values => presentationConfiguration({_st_inapp_presentation: {notification: values}}, 'notification');
  for (const value of [-1, Infinity, NaN]) {
    expect(settings({display_duration: value, corner_radius: value, outer_spacing: value, backdrop_opacity: value})).toEqual({});
  }
  expect(settings({backdrop_opacity: 1.01})).toEqual({});
  expect(settings({display_duration: 0, corner_radius: 0, outer_spacing: 0, backdrop_opacity: 0})).toEqual({
    display_duration: 0, corner_radius: 0, outer_spacing: 0, backdrop_opacity: 0,
  });
  expect(settings({backdrop_opacity: 1})).toEqual({backdrop_opacity: 1});
});
