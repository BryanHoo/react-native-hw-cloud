import React, { useEffect } from 'react';
import { StyleSheet, View, Text } from 'react-native';
import { upload } from 'react-native-hw-cloud';

export default function App() {
  const test = () => {
    upload({
      endPoint: 'a',
      ak: 'a',
      sk: 'a',
      token: 'a',
      bucketName: 'a',
      objectName: 'a',
      filePath: 'a',
    })
      .then((data) => {
        console.log(data);
      })
      .catch((err) => {
        console.log(err);
      });
  };

  useEffect(() => {
    test();
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
