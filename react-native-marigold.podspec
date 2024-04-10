require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "10.0" }

  s.source       = { :git => "https://github.com/sailthru/sailthru-mobile-react-native-sdk.git", :tag => "v#{s.version}" }
  s.source_files = "ios/*.{h,m}"

  s.dependency 'Marigold', '15.1.0'
  s.dependency 'Marigold-Extension', '15.1.0'
  s.dependency 'React-Core'
end