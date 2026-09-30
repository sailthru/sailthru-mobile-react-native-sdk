require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
marigold_version = File.read(File.join(__dir__, 'ios', '.marigold-ios-version')).strip

Pod::Spec.new do |s|
  s.name         = package['name']
  s.version      = package['version']
  s.summary      = package['description']

  s.authors      = package['author']
  s.homepage     = package['homepage']
  s.platforms    = { :ios => "17.6" }

  s.source       = { :git => "https://github.com/sailthru/sailthru-mobile-react-native-sdk.git", :tag => "v#{s.version}" }
  s.source_files = "ios/**/*.{h,m,mm,cpp}"

  spm_dependency(s,
    url: 'https://github.com/sailthru/sailthru-mobile-ios-sdk.git',
    requirement: { kind: 'exactVersion', version: marigold_version },
    products: ['Marigold', 'MarigoldExtension']
  )

  install_modules_dependencies(s)
end
