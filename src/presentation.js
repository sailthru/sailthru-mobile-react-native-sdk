// Decode the portable payload without mutating the message sent back to native code.
function presentationConfiguration(message, kind) {
  const payload = message?._st_inapp_presentation;
  if (!payload || typeof payload !== 'object' || Array.isArray(payload) ||
      (Object.prototype.hasOwnProperty.call(payload, 'version') && payload.version !== 1)) return null;
  const fields = payload[kind === 'notification' ? 'notification' : 'full_screen'];
  if (!fields || typeof fields !== 'object' || Array.isArray(fields)) return null;
  const result = {};
  const enums = {
    style: ['banner', 'modal', 'full_screen'], banner_position: ['top', 'bottom'],
    link_behavior: ['follow_link', 'show_message_with_button'], image_content_mode: ['aspect_fit', 'aspect_fill'],
  };
  for (const [key, options] of Object.entries(enums)) if (options.includes(fields[key])) result[key] = fields[key];
  for (const key of ['backdrop_opacity', 'corner_radius', 'outer_spacing', 'display_duration']) {
    const value = fields[key];
    if (typeof value === 'number' && Number.isFinite(value) && value >= 0 &&
        (key !== 'backdrop_opacity' || value <= 1)) result[key] = value;
  }
  for (const key of ['shows_image', 'shows_close_button', 'allows_gesture_dismissal']) {
    if (typeof fields[key] === 'boolean') result[key] = fields[key];
  }
  if (typeof fields.link_button_title === 'string') result.link_button_title = fields.link_button_title;
  const validColor = value => typeof value === 'string' && /^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$/.test(value);
  for (const key of ['background_color', 'title_color', 'body_text_color', 'close_button_color', 'backdrop_color']) {
    const value = fields[key];
    if (validColor(value)) result[key] = value;
    else if (value && !Array.isArray(value) && validColor(value.light) && validColor(value.dark)) result[key] = {light: value.light, dark: value.dark};
  }
  const weights = ['ultralight', 'thin', 'light', 'regular', 'medium', 'semibold', 'bold', 'heavy', 'black'];
  for (const key of ['title_font', 'body_font']) {
    const value = fields[key];
    if (!value || typeof value !== 'object' || Array.isArray(value) ||
        typeof value.size !== 'number' || !Number.isFinite(value.size) || value.size < 1 || value.size > 200 ||
        (Object.prototype.hasOwnProperty.call(value, 'weight') && !weights.includes(value.weight)) ||
        (Object.prototype.hasOwnProperty.call(value, 'name') && (typeof value.name !== 'string' || !value.name.trim()))) continue;
    result[key] = {size: value.size};
    if (value.name !== undefined) result[key].name = value.name;
    if (value.weight !== undefined) result[key].weight = value.weight;
    else if (value.name === undefined) result[key].weight = key === 'title_font' ? 'semibold' : 'regular';
  }
  return result;
}
module.exports = presentationConfiguration;
