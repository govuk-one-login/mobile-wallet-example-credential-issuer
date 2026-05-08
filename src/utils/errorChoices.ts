// Array of error choices for the UI "throwError" component.
// Each object represents a selectable option, with:
// - 'value': the code to be submitted,
// - 'text': the label shown to the user,
// - 'selected': (optional) marks the default selection.
export const ERROR_CHOICES = [
  { value: "", text: "No error", selected: true },
  { value: "ERROR:401", text: "401" },
  { value: "ERROR:CLIENT", text: "400 (invalid client)" },
  { value: "ERROR:GRANT", text: "400 (invalid grant)" },
  { value: "ERROR:500", text: "500" },
];
