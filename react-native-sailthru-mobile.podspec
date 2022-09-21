require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "10.0" }

  s.source       = { :git => "https://github.com/carnivalmobile/carnival-sdk-react-native.git", :tag => "v#{s.version}" }
  s.source_files = "ios/*.{h,m}"

  s.dependency 'SailthruMobile', '13.0.3'
  s.dependency 'SailthruMobile-Extension', '13.0.3'
  s.dependency 'React-Core'
end