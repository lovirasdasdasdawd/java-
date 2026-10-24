"""Three original periodic DSP atmospheres. Requires numpy/scipy/ffmpeg; no sampled recordings."""
from pathlib import Path
import numpy as np
from scipy.io import wavfile
import subprocess,tempfile,json
rate=22050;seconds=32;n=rate*seconds;u=np.arange(n)/n;freq=np.fft.rfftfreq(n,1/rate)
root=Path(__file__).resolve().parents[1];out=root/'fabric/src/main/resources/assets/donuttracker/sounds/ocean'
def noise(rng,lo,hi,tilt):
    sp=(rng.normal(size=len(freq))+1j*rng.normal(size=len(freq)))*(1-np.exp(-(freq/lo)**2))*np.exp(-(freq/hi)**2)/np.maximum(freq,20)**tilt
    sp[0]=0;sp[-1]=sp[-1].real;v=np.fft.irfft(sp,n);return v/np.std(v)
def tone(hz):return np.sin(2*np.pi*round(hz*seconds)*u)
def bell(hz,centre,width):
    dist=(u-centre+.5)%1-.5;return (tone(hz)+.24*tone(hz*2.76)+.09*tone(hz*5.4))*np.exp(-(dist/width)**2)
names=['ember_wind','marsh_night','night_rail']
for index,name in enumerate(names):
    rng=np.random.default_rng(11200+index)
    if name=='ember_wind':
        signal=noise(rng,45,650,.7)*.022+noise(rng,600,3800,.3)*(.012+.008*np.sin(2*np.pi*3*u)**2)
        for peak in [.12,.29,.54,.81]:signal+=noise(rng,1200,6500,.1)*.008*np.exp(-(((u-peak+.5)%1-.5)/.005)**2)
    elif name=='marsh_night':
        signal=noise(rng,160,2800,.6)*.017
        chirp=(.5+.5*np.sin(2*np.pi*64*u))**8
        signal+=tone(2870)*chirp*(.006+.004*np.sin(2*np.pi*3*u)**2)
        signal+=tone(175)*np.exp(-(((u-.55+.5)%1-.5)/.025)**2)*.01
    else:
        rhythm=(.5+.5*np.cos(2*np.pi*40*u))**12
        signal=noise(rng,45,650,.7)*.021+noise(rng,180,3000,.3)*(.006+.014*rhythm)+noise(rng,900,5500,.2)*.008
    signal*=.038/np.sqrt(np.mean(signal**2));assert np.max(np.abs(signal))<.65
    with tempfile.TemporaryDirectory() as td:
        wav=Path(td)/'loop.wav';wavfile.write(wav,rate,(signal*32767).astype(np.int16))
        subprocess.run(['ffmpeg','-v','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','4','-map_metadata','-1',str(out/(name+'.ogg'))],check=True)
    print(name,'32-second original loop; peak',round(float(np.max(np.abs(signal))),3))
p=out.parent.parent/'sounds.json';data=json.loads(p.read_text())
for name in names:data['ocean.'+name]={'sounds':[{'name':'donuttracker:ocean/'+name,'stream':True}]}
p.write_text(json.dumps(data,indent=2)+'\n')
