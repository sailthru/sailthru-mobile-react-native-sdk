require 'json'

ios_dir = __dir__
expected = File.read(File.join(ios_dir, '.marigold-ios-version')).strip
project_path = File.join(ios_dir, 'MarigoldSDKReactNative.xcodeproj', 'project.pbxproj')
project = JSON.parse(IO.popen(['plutil', '-convert', 'json', '-o', '-', project_path], &:read))
package = project.fetch('objects').values.find do |reference|
  reference['isa'] == 'XCRemoteSwiftPackageReference' && reference['repositoryURL'] == 'https://github.com/sailthru/sailthru-mobile-ios-sdk.git'
end
actual = package&.dig('requirement', 'version')
abort "Marigold Xcode package version #{actual || '(missing)'} must match ios/.marigold-ios-version (#{expected})" unless package&.dig('requirement', 'kind') == 'exactVersion' && actual == expected

resolved_path = File.join(ios_dir, 'MarigoldSDKReactNative.xcworkspace', 'xcshareddata', 'swiftpm', 'Package.resolved')
resolved = JSON.parse(File.read(resolved_path))
pin = resolved.fetch('pins').find { |entry| entry['identity'] == 'sailthru-mobile-ios-sdk' }
actual = pin&.dig('state', 'version')
abort "Marigold resolved version #{actual || '(missing)'} must match ios/.marigold-ios-version (#{expected})" unless actual == expected
