require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
marigold_version = File.read(File.join(__dir__, 'ios', '.marigold-ios-version'))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "15.6" }

  s.source       = { :git => "https://github.com/sailthru/sailthru-mobile-react-native-sdk.git", :tag => "v#{s.version}" }
  s.source_files = "ios/**/*.{h,m,mm,cpp}"

  s.dependency 'Marigold', marigold_version
  s.dependency 'Marigold-Extension', marigold_version

  install_modules_dependencies(s)
end