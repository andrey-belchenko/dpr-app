export function getHostUrl(): string {
  let val = `${window.location.protocol}//${window.location.hostname}`;
  if (window.location.port) {
    val += ":" + window.location.port;
  }
  return val;
}
