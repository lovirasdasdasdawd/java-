"""Three original periodic DSP soundscapes. No sampled or model-generated audio."""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess,tempfile,json
rate=22050;seconds=32;n=rate*seconds;u=np.arange(n)/n
freq=np.fft.rfftfreq(n,1/rate)
rng=np.random.default_rng(19019)
def tone(hz):return np.sin(2*np.pi*round(hz*seconds)*u)
def noise(lo,hi,tilt):
    sp=(rng.normal(size=len(freq))+1j*rng.normal(size=len(freq)))*(1-np.exp(-(freq/lo)**2))*np.exp(-(freq/hi)**2)/np.maximum(freq,20)**tilt
    sp[0]=0;sp[-1]=sp[-1].real
    v=np.fft.irfft(sp,n);return v/np.std(v)
out=Path(__file__).resolve().parents[1]/'fabric/src/main/resources/assets/donuttracker/sounds/ocean'
for name in ['deep_space','city_rain','forest_breeze']:
    if name=='deep_space':
        signal=noise(30,800,.7)*.009
        for i,hz in enumerate([65.406,98,130.813,196,293.665]):signal+=tone(hz)*(.009+.004*np.sin(2*np.pi*(i%3+1)*u+i))
        signal+=tone(784)*np.exp(-(((u-.35+.5)%1-.5)/.045)**2)*.008
    elif name=='city_rain':
        signal=noise(500,6500,.18)*.037+noise(45,700,.6)*(.012+.008*np.sin(2*np.pi*u)**2)
        # Distant traffic swell, no speech, horns or sudden loud transients.
        signal+=noise(70,1100,.4)*(.015*np.exp(-(((u-.65+.5)%1-.5)/.13)**2))
    else:
        signal=noise(120,3800,.4)*(.018+.014*np.sin(2*np.pi*3*u+.6*np.sin(2*np.pi*u))**2)
        for i,peak in enumerate([.11,.32,.56,.79]):
            dist=(u-peak+.5)%1-.5;env=np.exp(-(dist/.009)**2)
            phase=2*np.pi*(round((1500+i*190)*seconds)*u+18*np.sin(2*np.pi*7*u))
            signal+=np.sin(phase)*env*.018
    signal*=.045/np.sqrt(np.mean(signal**2));assert np.max(np.abs(signal))<.65
    with tempfile.TemporaryDirectory() as td:
        wav=Path(td)/'loop.wav';wavfile.write(wav,rate,(signal*32767).astype(np.int16))
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out/(name+'.ogg'))],check=True)
    print(name,'32s RMS .045 peak',round(float(np.max(np.abs(signal))),3))
p=out.parent.parent/'sounds.json';data=json.loads(p.read_text())
for name in ['deep_space','city_rain','forest_breeze']:data['ocean.'+name]={'sounds':[{'name':'donuttracker:ocean/'+name,'stream':True}]}
p.write_text(json.dumps(data,indent=2)+'\n')
